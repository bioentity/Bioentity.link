package com.insilico.dmc

import com.insilico.dmc.publication.Publication
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.jcraft.jsch.SftpException
import org.springframework.beans.factory.annotation.Value

class NovaTechsetService {

    final String host = "sftp.novatechset.com"
    final int port = 22
    final String user = "GSA_Database"

    @Value('${sftp.secret:NOPASSWORD}') String sftpPassword

    /**
     * Upload the XML to Nova Techset using SFTP
     */
    def sendToNovaTechset(String xml, Publication publication) {

        JSch jSch = new JSch()
        Session session = jSch.getSession(user, host, port)

        if(sftpPassword == "NOPASSWORD") {
            throw new SftpException(ChannelSftp.SSH_FX_PERMISSION_DENIED, "Sheridan sftp password not set in env")
        }

        session.setPassword(sftpPassword)
        session.setConfig("StrictHostKeyChecking", "no")

        ChannelSftp channel
        try {
            session.connect()
            channel = (ChannelSftp) session.openChannel("sftp")
            channel.connect()

            channel.put(new ByteArrayInputStream(xml.bytes),publication.fileName,ChannelSftp.OVERWRITE)
//        os.write(xml.getBytes())
        } catch (e) {
            log.error("Problem uploading the file")
            throw e
        } finally {
            channel?.disconnect()
            session.disconnect()
        }
    }

}
