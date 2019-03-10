#!/bin/bash

sudo ng build --prod -env demo2
sudo rm -rf /var/www/html/*
sudo cp -r dist/* /var/www/html

