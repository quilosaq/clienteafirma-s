/*
 * Este fichero forma parte del Cliente @firma.
 * El Cliente @firma es un aplicativo de libre distribucion cuyo codigo fuente puede ser consultado
 * y descargado desde www.ctt.map.es.
 * Copyright 2009,2010,2011 Gobierno de Espana
 * Este fichero se distribuye bajo  bajo licencia GPL version 2  segun las
 * condiciones que figuran en el fichero 'licence' que se acompana. Si se distribuyera este
 * fichero individualmente, deben incluirse aqui las condiciones expresadas alli.
 */

package es.gob.afirma.signers.cms;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;

import es.gob.afirma.core.AOException;
import es.gob.afirma.core.AOInvalidFormatException;
import es.gob.afirma.core.AOUnsupportedSignFormatException;
import es.gob.afirma.core.misc.MimeHelper;
import es.gob.afirma.core.signers.AOSignConstants;
import es.gob.afirma.core.signers.AOSignConstants.CounterSignTarget;
import es.gob.afirma.core.signers.AOSigner;
import es.gob.afirma.core.signers.beans.AOSignInfo;
import es.gob.afirma.core.util.tree.AOTreeModel;
import es.gob.afirma.signers.pkcs7.ExtractMimeType;
import es.gob.afirma.signers.pkcs7.GenSignedData;
import es.gob.afirma.signers.pkcs7.ObtainContentSignedData;
import es.gob.afirma.signers.pkcs7.P7ContentSignerParameters;
import es.gob.afirma.signers.pkcs7.ReadNodesTree;

/** Manejador de firmas binarias CMS. Par&aacute;metros adicionales aceptados
 * para las operaciones de firma:<br>
 * <dl>
 * <dt>mode</dt>
 * <dd>Modo de firma a usar (Expl&iacute;cita o Impl&iacute;cita)</dd>
 * <dt>applySystemDate</dt>
 * <dd><code>true</code> si se desea usar la hora y fecha del sistema como hora y fecha de firma, <code>false</code> en caso contrario
 * <dt>precalculatedHashAlgorithm</dt>
 * <dd>Algoritmo de huella digital cuando esta se proporciona precalculada</dd>
 * </dl>
 * @version 0.1 */
public final class AOCMSSigner implements AOSigner {

    private String dataType = null;
    private final Map<String, byte[]> atrib = new HashMap<String, byte[]>();
    private final Map<String, byte[]> uatrib = new HashMap<String, byte[]>();
    
    private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

    public byte[] sign(final byte[] data, String algorithm, final PrivateKeyEntry keyEntry, Properties extraParams) throws AOException {

        if (extraParams == null) {
            extraParams = new Properties();
        }

        if (algorithm.equalsIgnoreCase("RSA")) { //$NON-NLS-1$
            algorithm = AOSignConstants.SIGN_ALGORITHM_SHA1WITHRSA;
        }
        else if (algorithm.equalsIgnoreCase("DSA")) { //$NON-NLS-1$
            algorithm = AOSignConstants.SIGN_ALGORITHM_SHA1WITHDSA;
        }

        final String precalculatedDigest = extraParams.getProperty("precalculatedHashAlgorithm"); //$NON-NLS-1$

        byte[] messageDigest = null;
        if (precalculatedDigest != null) {
            messageDigest = data;
        }

        X509Certificate[] xCerts = new X509Certificate[0];
        final Certificate[] certs = keyEntry.getCertificateChain();
        if (certs != null && (certs instanceof X509Certificate[])) {
            xCerts = (X509Certificate[]) certs;
        }
        else {
            final Certificate cert = keyEntry.getCertificate();
            if (cert instanceof X509Certificate) {
                xCerts = new X509Certificate[] {
                                                (X509Certificate) cert
                };
            }
        }

        final P7ContentSignerParameters csp = new P7ContentSignerParameters(data, algorithm, xCerts);

        // tipos de datos a firmar.
        if (this.dataType == null) {
            this.dataType = PKCSObjectIdentifiers.data.getId();
        }

        final String mode = extraParams.getProperty("mode", AOSignConstants.DEFAULT_SIGN_MODE); //$NON-NLS-1$

        try {
            final boolean omitContent = mode.equals(AOSignConstants.SIGN_MODE_EXPLICIT) || precalculatedDigest != null;
            return new GenSignedData().generateSignedData(csp,
                                                          omitContent,
                                                          Boolean.parseBoolean(extraParams.getProperty("applySystemDate", "true")), //$NON-NLS-1$ //$NON-NLS-2$
                                                          this.dataType,
                                                          keyEntry,
                                                          this.atrib,
                                                          this.uatrib,
                                                          messageDigest);
        }
        catch (final Exception e) {
            throw new AOException("Error generando la firma PKCS#7", e); //$NON-NLS-1$
        }
    }

    public byte[] cosign(final byte[] data, final byte[] sign, String algorithm, final PrivateKeyEntry keyEntry, Properties extraParams) throws AOException {

        if (extraParams == null) {
            extraParams = new Properties();
        }

        if (algorithm.equalsIgnoreCase("RSA")) { //$NON-NLS-1$
            algorithm = AOSignConstants.SIGN_ALGORITHM_SHA1WITHRSA;
        }
        else if (algorithm.equalsIgnoreCase("DSA")) { //$NON-NLS-1$
            algorithm = AOSignConstants.SIGN_ALGORITHM_SHA1WITHDSA;
        }

        final String precalculatedDigest = extraParams.getProperty("precalculatedHashAlgorithm"); //$NON-NLS-1$

        byte[] messageDigest = null;
        if (precalculatedDigest != null) {
            messageDigest = data;
        }

        X509Certificate[] xCerts = new X509Certificate[0];
        final Certificate[] certs = keyEntry.getCertificateChain();
        if (certs != null && (certs instanceof X509Certificate[])) {
            xCerts = (X509Certificate[]) certs;
        }
        else {
            final Certificate cert = keyEntry.getCertificate();
            if (cert instanceof X509Certificate) {
                xCerts = new X509Certificate[] {
                                                (X509Certificate) cert
                };
            }
        }

        final P7ContentSignerParameters csp = new P7ContentSignerParameters(data, algorithm, xCerts);

        // tipos de datos a firmar.
        if (this.dataType == null) {
            this.dataType = PKCSObjectIdentifiers.data.getId();
        }

        final String mode = extraParams.getProperty("mode", AOSignConstants.DEFAULT_SIGN_MODE); //$NON-NLS-1$

        final boolean omitContent = mode.equals(AOSignConstants.SIGN_MODE_EXPLICIT) || precalculatedDigest != null;

        // Si la firma que nos introducen es SignedData
        final boolean signedData = CMSHelper.isCMSValid(sign, AOSignConstants.CMS_CONTENTTYPE_SIGNEDDATA);
        if (signedData) {
            try {
                return new CoSigner().coSigner(csp, sign, omitContent, this.dataType, keyEntry, this.atrib, this.uatrib, messageDigest);
            }
            catch (final Exception e) {
                throw new AOException("Error generando la Cofirma PKCS#7", e); //$NON-NLS-1$
            }
        }
        // Si la firma que nos introducen es SignedAndEnvelopedData
        try {
            // El parametro omitContent no tiene sentido en un signed and
            // envelopedData.
            return new CoSignerEnveloped().coSigner(csp, sign, this.dataType, keyEntry, this.atrib, this.uatrib, messageDigest);
        }
        catch (final Exception e) {
            throw new AOException("Error generando la Cofirma PKCS#7", e); //$NON-NLS-1$
        }
    }

    public byte[] cosign(final byte[] sign, String algorithm, final PrivateKeyEntry keyEntry, final Properties extraParams) throws AOException {

        if (algorithm.equalsIgnoreCase("RSA")) { //$NON-NLS-1$
            algorithm = AOSignConstants.SIGN_ALGORITHM_SHA1WITHRSA;
        }
        else if (algorithm.equalsIgnoreCase("DSA")) { //$NON-NLS-1$
            algorithm = AOSignConstants.SIGN_ALGORITHM_SHA1WITHDSA;
        }

        // tipos de datos a firmar.
        if (this.dataType == null) {
            this.dataType = PKCSObjectIdentifiers.data.getId();
        }

        // Algoritmo de firma.
        final String typeAlgorithm = algorithm;

        // Array de certificados
        X509Certificate[] aCertificados = new X509Certificate[0];
        final Certificate[] certs = keyEntry.getCertificateChain();
        if (certs != null && (certs instanceof X509Certificate[])) {
            aCertificados = (X509Certificate[]) certs;
        }
        else {
            final Certificate cert = keyEntry.getCertificate();
            if (cert instanceof X509Certificate) {
                aCertificados = new X509Certificate[] {
                                                       (X509Certificate) cert
                };
            }
        }

        // Si la firma que nos introducen es SignedData
        if (CMSHelper.isCMSValid(sign, AOSignConstants.CMS_CONTENTTYPE_SIGNEDDATA)) {
            // Cofirma de la firma usando unicamente el fichero de firmas.
            try {
                return new CoSigner().coSigner(typeAlgorithm, aCertificados, sign, this.dataType, keyEntry, this.atrib, this.uatrib, null // null
                                                                                                                           // porque
                                                                                                                           // no
                                                                                                                           // nos
                                                                                                                           // pueden
                                                                                                                           // dar
                                                                                                                           // un
                                                                                                                           // hash
                                                                                                                           // en
                                                                                                                           // este
                                                                                                                           // metodo,
                                                                                                                           // tendr�a
                                                                                                                           // que
                                                                                                                           // ser
                                                                                                                           // en el
                                                                                                                           // que
                                                                                                                           // incluye
                                                                                                                           // datos
                );
            }
            catch (final Exception e) {
                throw new AOException("Error generando la Cofirma PKCS#7", e); //$NON-NLS-1$
            }
        }
        // Si la firma que nos introducen es SignedAndEnvelopedData

        // Cofirma de la firma usando unicamente el fichero de firmas.
        try {
            return new CoSignerEnveloped().coSigner(typeAlgorithm, aCertificados, sign, this.dataType, keyEntry, this.atrib, this.uatrib, null // null porque no nos
                                                                                                                                // pueden dar un hash
                                                                                                                                // en este
                                                                                                                                // metodo, tendr�a que
                                                                                                                                // ser en el que
                                                                                                                                // incluye datos
            );
        }
        catch (final Exception e) {
            throw new AOException("Error generando la Cofirma PKCS#7", e); //$NON-NLS-1$
        }
    }

    public byte[] countersign(final byte[] sign,
                              String algorithm,
                              final CounterSignTarget targetType,
                              final Object[] targets,
                              final PrivateKeyEntry keyEntry,
                              final Properties extraParams) throws AOException {

        if (algorithm.equalsIgnoreCase("RSA")) { //$NON-NLS-1$
            algorithm = AOSignConstants.SIGN_ALGORITHM_SHA1WITHRSA;
        }
        else if (algorithm.equalsIgnoreCase("DSA")) { //$NON-NLS-1$
            algorithm = AOSignConstants.SIGN_ALGORITHM_SHA1WITHDSA;
        }

        X509Certificate[] xCerts = new X509Certificate[0];
        final Certificate[] certs = keyEntry.getCertificateChain();
        if (certs != null && (certs instanceof X509Certificate[])) {
            xCerts = (X509Certificate[]) certs;
        }
        else {
            final Certificate cert = keyEntry.getCertificate();
            if (cert instanceof X509Certificate) {
                xCerts = new X509Certificate[] {
                                                (X509Certificate) cert
                };
            }
        }

        final P7ContentSignerParameters csp = new P7ContentSignerParameters(sign, algorithm, xCerts);

        // tipos de datos a firmar.
        if (this.dataType == null) {
            this.dataType = PKCSObjectIdentifiers.data.getId();
        }

        // Datos firmados.
        byte[] dataSigned = null;

        // Si la firma que nos introducen es SignedData

        if (CMSHelper.isCMSValid(sign, AOSignConstants.CMS_CONTENTTYPE_SIGNEDDATA)) {
            try {
                // CASO DE FIRMA DE ARBOL
                if (targetType == CounterSignTarget.Tree) {
                    final int[] nodes = {
                        0
                    };
                    dataSigned = new CounterSigner().counterSigner(csp, sign, CounterSignTarget.Tree, nodes, keyEntry, this.dataType, this.atrib, this.uatrib);
                }
                // CASO DE FIRMA DE HOJAS
                else if (targetType == CounterSignTarget.Leafs) {
                    final int[] nodes = {
                        0
                    };
                    dataSigned = new CounterSigner().counterSigner(csp, sign, CounterSignTarget.Leafs, nodes, keyEntry, this.dataType, this.atrib, this.uatrib);
                }
                // CASO DE FIRMA DE NODOS
                else if (targetType == CounterSignTarget.Nodes) {
                    int[] nodesID = new int[targets.length];
                    for (int i = 0; i < targets.length; i++) {
                        nodesID[i] = ((Integer) targets[i]).intValue();
                    }
                    nodesID = new ReadNodesTree().simplyArray(nodesID);
                    dataSigned = new CounterSigner().counterSigner(csp, sign, CounterSignTarget.Nodes, nodesID, keyEntry, this.dataType, this.atrib, this.uatrib);
                }
                // CASO DE FIRMA DE NODOS DE UNO O VARIOS FIRMANTES
                else if (targetType == CounterSignTarget.Signers) {

                    // clase que lee los nodos de un fichero firmado (p7s)
                    final String[] signers = new String[targets.length];
                    for (int i = 0; i < targets.length; i++) {
                        signers[i] = (String) targets[i];
                    }
                    final ReadNodesTree rn2 = new ReadNodesTree();
                    final int[] nodes2 = rn2.readNodesFromSigners(signers, sign);
                    dataSigned = new CounterSigner().counterSigner(csp, sign, CounterSignTarget.Signers, nodes2, keyEntry, this.dataType, this.atrib, this.uatrib);

                }
            }
            catch (final Exception e) {
                throw new AOException("Error generando la Contrafirma PKCS#7", e); //$NON-NLS-1$
            }
        }
        // Si la firma es SignedAndEnveloped
        else {

            try {
                // CASO DE FIRMA DE ARBOL
                if (targetType == CounterSignTarget.Tree) {
                    final int[] nodes = {
                        0
                    };
                    dataSigned =
                            new CounterSignerEnveloped().counterSignerEnveloped(csp,
                                                                                sign,
                                                                                CounterSignTarget.Tree,
                                                                                nodes,
                                                                                keyEntry,
                                                                                this.dataType,
                                                                                this.atrib,
                                                                                this.uatrib);
                }
                // CASO DE FIRMA DE HOJAS
                else if (targetType == CounterSignTarget.Leafs) {
                    final int[] nodes = {
                        0
                    };
                    dataSigned =
                            new CounterSignerEnveloped().counterSignerEnveloped(csp,
                                                                                sign,
                                                                                CounterSignTarget.Leafs,
                                                                                nodes,
                                                                                keyEntry,
                                                                                this.dataType,
                                                                                this.atrib,
                                                                                this.uatrib);
                }
                // CASO DE FIRMA DE NODOS
                else if (targetType == CounterSignTarget.Nodes) {
                    int[] nodesID = new int[targets.length];
                    for (int i = 0; i < targets.length; i++) {
                        nodesID[i] = ((Integer) targets[i]).intValue();
                    }
                    nodesID = new ReadNodesTree().simplyArray(nodesID);
                    dataSigned =
                            new CounterSignerEnveloped().counterSignerEnveloped(csp,
                                                                                sign,
                                                                                CounterSignTarget.Nodes,
                                                                                nodesID,
                                                                                keyEntry,
                                                                                this.dataType,
                                                                                this.atrib,
                                                                                this.uatrib);
                }
                // CASO DE FIRMA DE NODOS DE UNO O VARIOS FIRMANTES
                else if (targetType == CounterSignTarget.Signers) {

                    // clase que lee los nodos de un fichero firmado (p7s)
                    final String[] signers = new String[targets.length];
                    for (int i = 0; i < targets.length; i++){
                        signers[i] = (String) targets[i];
                    }
                    dataSigned =
                            new CounterSignerEnveloped().counterSignerEnveloped(csp,
                                                                                sign,
                                                                                CounterSignTarget.Signers,
                                                                                new ReadNodesTree().readNodesFromSigners(signers, sign),
                                                                                keyEntry,
                                                                                this.dataType,
                                                                                this.atrib,
                                                                                this.uatrib);

                }
            }
            catch (final Exception e) {
                throw new AOException("Error generando la Contrafirma PKCS#7", e); //$NON-NLS-1$
            }

        }

        return dataSigned;
    }

    public AOTreeModel getSignersStructure(final byte[] sign, final boolean asSimpleSignInfo) {
        final ReadNodesTree Rn = new ReadNodesTree();
        try {
            return Rn.readNodesTree(sign, asSimpleSignInfo);
        }
        catch (final Exception ex) {
            LOGGER.severe(ex.toString());
        }
        return null;
    }

    public boolean isSign(final byte[] signData) {
        if (signData == null) {
            LOGGER.warning("Se han introducido datos nulos para su comprobacion"); //$NON-NLS-1$
            return false;
        }

        // Comprobamos la validez
        boolean signed = CMSHelper.isCMSValid(signData, AOSignConstants.CMS_CONTENTTYPE_SIGNEDDATA);
        if (!signed) {
            signed = CMSHelper.isCMSValid(signData, AOSignConstants.CMS_CONTENTTYPE_SIGNEDANDENVELOPEDDATA);
        }

        return signed;
    }

    public boolean isValidDataFile(final byte[] data) {
        if (data == null) {
            LOGGER.warning("Se han introducido datos nulos para su comprobacion"); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    public String getDataMimeType(final byte[] signData) throws AOUnsupportedSignFormatException {

        String numOid = ""; //$NON-NLS-1$
        String oid = ""; //$NON-NLS-1$

        // Comprobamos que sea una firma valida
        try {
            this.isSign(signData);
        }
        catch (final Exception e1) {
            throw new AOUnsupportedSignFormatException("No es un tipo de firma valido"); //$NON-NLS-1$
        }

        // Extraemos el mimetype
        final ExtractMimeType extract = new ExtractMimeType();
        numOid = extract.extractMimeType(signData);

        // Transformamos el OID a mimeType
        oid = MimeHelper.transformOidToMimeType(numOid);

        return oid;
    }

    /** A&ntilde;ade un atributo firmado al formato de firma seleccionado. Este
     * formato debe reconocer el OID especificado, siendo el atributo value su
     * valor como cadena de texto.
     * @param oid
     *        Object Identifier. Identificador del objeto a introducir.
     * @param value
     *        Valor asignado */
    public void addSignedAttribute(final String oid, final byte[] value) {
        this.atrib.put(oid, value);
    }

    /** A&ntilde;ade un atributo no firmado al formato de firma seleccionado.
     * @param oid
     *        Object Identifier. Identificador del atributo a introducir.
     * @param value
     *        Valor asignado */
    public void addUnsignedAttribute(final String oid, final byte[] value) {
        this.uatrib.put(oid, value);
    }

    public void setDataObjectFormat(final String description, final String objectIdentifier, final String mimeType, final String encoding) {

        // No permitimos el cambio del tipo de dato. CMS/CAdES establece que
        // siempre
        // sera de tipo DATA
        // this.dataType = objectIdentifier;

    }

    public byte[] getData(final byte[] signData) throws AOInvalidFormatException, AOException {

        if (signData == null) {
            throw new IllegalArgumentException("Se han introducido datos nulos para su comprobacion"); //$NON-NLS-1$
        }

        if (!CMSHelper.isCMSValid(signData)) {
            throw new AOInvalidFormatException("Los datos introducidos no se corresponden con un objeto de firma"); //$NON-NLS-1$
        }

        return new ObtainContentSignedData().obtainData(signData);
    }

    public String getSignedName(final String originalName, final String inText) {
        return originalName + (inText != null ? inText : "") + ".csig";  //$NON-NLS-1$//$NON-NLS-2$
    }

    public AOSignInfo getSignInfo(final byte[] signData) throws AOInvalidFormatException, AOException {

        if (signData == null) {
            throw new IllegalArgumentException("No se han introducido datos para analizar"); //$NON-NLS-1$
        }

        if (!isSign(signData)) {
            throw new AOInvalidFormatException("Los datos introducidos no se corresponden con un objeto de firma"); //$NON-NLS-1$
        }

        final AOSignInfo signInfo = new AOSignInfo(AOSignConstants.SIGN_FORMAT_CMS);
        // Aqui podria venir el analisis de la firma buscando alguno de los
        // otros datos de relevancia
        // que se almacenan en el objeto AOSignInfo

        return signInfo;
    }
}
