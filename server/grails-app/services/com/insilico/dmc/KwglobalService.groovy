package com.insilico.dmc

import com.insilico.dmc.publication.Publication
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.jcraft.jsch.SftpException
import org.springframework.beans.factory.annotation.Value

class KwglobalService {

    final String host = "ftp.kwglobal.com"
    final int port = 2222
    final String user = "AGS"

    @Value('${sftp.secret:NOPASSWORD}') String sftpPassword

    /**
     * Upload the XML to Sheridan using SFTP
     */
    def sendToKWGlobal(String xml, Publication publication) {

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
