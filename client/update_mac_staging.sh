#!/bin/bash

ng build --prod --env staging 
#sudo rm -rf /var/www/html/*
# local mac nginx target
sudo cp -r dist/* /Library/WebServer/Documents/

