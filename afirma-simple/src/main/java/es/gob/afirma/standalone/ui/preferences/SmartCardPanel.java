/* Copyright (C) 2022 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * You may contact the copyright holder at: soporte.afirma@seap.minhap.es
 */

package es.gob.afirma.standalone.ui.preferences;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import es.gob.afirma.core.misc.Platform;
import es.gob.afirma.core.ui.AOUIFactory;
import es.gob.afirma.standalone.SimpleAfirmaMessages;

/** Panel para el di&aacute;logo de configuraci&oacute; del listado de dominios seguros. */
final class SmartCardPanel extends JPanel {

	private static final long serialVersionUID = -6040435120676908406L;
	private static final int PREFERRED_WIDTH = 620;
	private static final int PREFERRED_HEIGHT = 210;

	static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	final JLabel cardnameLbl = new JLabel(SimpleAfirmaMessages.getString("SmartCardDialog.1")); //$NON-NLS-1$
	final JLabel controllerNameLbl = new JLabel(SimpleAfirmaMessages.getString("SmartCardDialog.2")); //$NON-NLS-1$
	final JTextField cardNameTxt = new JTextField();
	final JTextField controllerNameTxt = new JTextField();

	private SecureDomainsHandler eventsHandler;

	/** Constructor. */
	SmartCardPanel() {
		createUI();
	}

	/**
	 * Construimos visualmente el panel.
	 */
	private void createUI() {
		setLayout(new GridBagLayout());
		setMinimumSize(new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT));

		this.cardnameLbl.setFocusable(true);
		this.cardNameTxt.setFocusable(true);
		this.controllerNameLbl.setFocusable(true);
		this.controllerNameLbl.setFocusable(true);
		this.controllerNameTxt.setEditable(false);

		final JButton selectFileButton = new JButton(SimpleAfirmaMessages.getString("SmartCardDialog.3")); //$NON-NLS-1$
		selectFileButton.setFocusable(true);

		selectFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final String[] exts;
                String extsDesc = SimpleAfirmaMessages.getString("SmartCardDialog.8"); //$NON-NLS-1$
                if (Platform.OS.WINDOWS.equals(Platform.getOS())) {
                    exts = new String[] { "dll" }; //$NON-NLS-1$
                    extsDesc = extsDesc + " (*.dll)"; //$NON-NLS-1$
                }
                else if (Platform.OS.MACOSX.equals(Platform.getOS())) {
                    exts = new String[] { "so", "dylib" }; //$NON-NLS-1$ //$NON-NLS-2$
                    extsDesc = extsDesc + " (*.dylib, *.so)"; //$NON-NLS-1$
                }
                else {
                    exts = new String[] { "so" }; //$NON-NLS-1$
                    extsDesc = extsDesc + " (*.so)"; //$NON-NLS-1$
                }
            	final String driverPath = AOUIFactory.getLoadFiles(
                 		SimpleAfirmaMessages.getString("SmartCardDialog.7"), //$NON-NLS-1$
                 		null,
                 		null,
                 		exts,
                 		extsDesc,
                 		false,
                 		false,
                 		null,
                 		this
             		)[0].getAbsolutePath();
            	SmartCardPanel.this.controllerNameTxt.setText(driverPath);
            }
            });

		final JButton connectCardButton = new JButton(SimpleAfirmaMessages.getString("SmartCardDialog.4")); //$NON-NLS-1$
		connectCardButton.setFocusable(true);

		connectCardButton.addActionListener(
        		ae -> PreferencesPanelKeystores.connectSmartCard(this)
		);

		// Colocamos los componentes en el panel
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.gridx = 0;
		c.gridy = 0;
		this.add(this.cardnameLbl, c);
		c.insets = new Insets(0, 0, 0, 0);
		c.gridy++;
		this.add(this.cardNameTxt, c);
		c.gridy++;
		this.add(this.controllerNameLbl, c);
		c.gridy++;
		this.add(this.controllerNameTxt, c);
		c.gridx++;
		this.add(selectFileButton, c);
		c.gridx = 0;
		c.gridy++;
		this.add(connectCardButton, c);
	}

	/**
	 * Guarda los datos establecidos en el panel.
	 * @throws ConfigurationException Cuando los datos introducidos no son validos.
	 */
	void saveData() throws ConfigurationException {

		this.eventsHandler.saveViewData();
	}

	public JTextField getCardNameTxt() {
		return this.cardNameTxt;
	}

	public JTextField getControllerNameTxt() {
		return this.controllerNameTxt;
	}

}
