sudo service tomcat8 stop 
sudo rm -rf /var/lib/tomcat/webapps/dmc*
sudo cp build/library/dmc* /var/lib/tomcat/webapps/dmc.war
