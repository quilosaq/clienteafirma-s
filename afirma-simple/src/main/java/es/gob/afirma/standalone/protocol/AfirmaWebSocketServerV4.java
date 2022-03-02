/* Copyright (C) 2022 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * You may contact the copyright holder at: soporte.afirma@seap.minhap.es
 */

package es.gob.afirma.standalone.protocol;

import java.util.Collections;

import javax.swing.Timer;

import org.java_websocket.WebSocket;

/**
 * Servidor para la comunicaci&oacute;n por <i>WebSocket</i> acorde a la versi&oacute;n 4
 * del protocolo.
 */
public final class AfirmaWebSocketServerV4 extends AfirmaWebSocketServer {

	/** Versi&oacute;n de protocolo. */
	private static final int PROTOCOL_VERSION = 4;

	/** Prefijo de las peticiones de eco. */
	private static final String ECHO_REQUEST_PREFIX = "echo="; //$NON-NLS-1$

	/** Sufijo de las peticiones de eco. */
	private static final String ECHO_REQUEST_SUFFIX = "@EOF"; //$NON-NLS-1$

	private static final String IDSESSION_PARAM_PREFIX = "idsession="; //$NON-NLS-1$

	/** Respuesta que se debe enviar ante las peticiones de echo correctas. */
	private static final String ECHO_OK_RESPONSE = "OK"; //$NON-NLS-1$

	private static Timer inactivityTimer;


	/**
	 * Genera un servidor websocket que atiende las peticiones del Cliente @firma.
	 * @param port Puerto a trav&eacute;s del que realizar la comunicaci&oacute;n.
	 */
	public AfirmaWebSocketServerV4(final int port, final String sessionId) {
		super(port, sessionId);
	}

	@Override
	public void onMessage(final WebSocket ws, final String message) {
		LOGGER.info("Recibimos una peticion en el socket"); //$NON-NLS-1$

		// Si aun corre el temporizador de inactividad, lo detenemos para que no
		// cierre la aplicacion
		if (inactivityTimer != null) {
			inactivityTimer.stop();
			inactivityTimer = null;
		}

		// Si se proporciono un ID de sesion al crear el socket y el ID del
		// mensaje enviado no coincide con el mismo, ignoraremos la peticion
		if (this.sessionId != null && !this.sessionId.equals(getSessionId(message))) {
			final String errorResponse = ProtocolInvocationLauncherErrorManager.getErrorMessage(
					ProtocolInvocationLauncherErrorManager.ERROR_INVALID_SESSION_ID);
			broadcast(errorResponse, Collections.singletonList(ws));

			return;
		}

		// Si recibimos en el socket un eco, lo respondemos con un OK
		if (message.startsWith(ECHO_REQUEST_PREFIX)) {
			broadcast(ECHO_OK_RESPONSE, Collections.singletonList(ws));
		}
		// Si recibimos cualquier cosa distinta de un eco, consideraremos que es una peticion de
		// operacion y la procesaremos como tal
		else {
			broadcast(ProtocolInvocationLauncher.launch(message, PROTOCOL_VERSION, true), Collections.singletonList(ws));
		}
	}

	/**
	 * Devuelve el valor del par&aacute;metro "idsession" del mensaje.
	 * @param message Mensaje.
	 * @return Par&aacute;metro "idsession" o {@code null} si no se encontr&oacute;.
	 */
	private static String getSessionId(final String message) {
		String id = null;
		final int idx = message.indexOf(IDSESSION_PARAM_PREFIX);
		if (idx > -1) {
			final int startIdx = idx + IDSESSION_PARAM_PREFIX.length();
			final int endIdx = message.indexOf("&", startIdx); //$NON-NLS-1$
			if (endIdx > -1) {
				id = message.substring(startIdx, endIdx);
			}
			else {
				id = message.substring(startIdx);
				if (id.endsWith(ECHO_REQUEST_SUFFIX)) {
					id = id.substring(0, id.length() - ECHO_REQUEST_SUFFIX.length());
				}
			}
		}

		return id;
	}
}
