#!/bin/bash

sudo ng build --prod -env bioentity
sudo rm -rf /var/www/html/*
sudo cp -r dist/* /var/www/html

