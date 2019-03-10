sudo service tomcat8 stop 
sudo rm -rf /var/lib/tomcat/webapps/dmc*
sudo cp build/libs/dmc-0.1.war /var/lib/tomcat8/webapps/dmc.war

