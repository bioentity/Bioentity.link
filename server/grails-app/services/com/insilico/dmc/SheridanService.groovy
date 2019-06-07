package com.insilico.dmc

import com.insilico.dmc.publication.Publication
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.jcraft.jsch.SftpException
import org.springframework.beans.factory.annotation.Value

class SheridanService {

    final String host = "sftp.dartmouthjournals.com"
    final int port = 22
    final String user = "FROM_BIOENTITY"

    @Value('${sftp.secret:NOPASSWORD}') String sftpPassword

    /**
     * Upload the XML to Sheridan using SFTP
     */
    def sendToSheridan(String xml, Publication publication) {

        JSch jSch = new JSch()
        Session session = jSch.getSession(user, host, port)

        if(sftpPassword == "NOPASSWORD") {
//            throw new SftpException(ChannelSftp.SSH_FX_PERMISSION_DENIED, "Sheridan sftp password not set in env")
		sftpPassword = "1obozi2!%a"
        }

        session.setPassword(sftpPassword)
        session.setConfig("StrictHostKeyChecking", "no")

        session.connect()
        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp")
        channel.connect()

        OutputStream os = channel.put(publication.fileName)
        os.write(xml.getBytes())

        channel.disconnect()
        session.disconnect()
    }

}
