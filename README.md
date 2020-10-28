

## Update instructions

Production branch is `production`, and should not be pushed as it contains the password.

Git changes from master via `git pull origin master`. 

Update in order:
- `cd client`
- `./update_bioentity.sh`
- `cd ../texture`
- `./update_production.sh``
- `cd ../server`
- Follow instrucions in the README.md for server


# BioEntity.Link Production update

- SSH into the server (this is how Nathan do, but you can login separately and `sudo su ubuntu`):
    
      ssh -i ~/.ssh/insilico-oregonv2.pem ubuntu@bioentity.link 
   
- go into code-base (every user is ubuntu I think?) and pull from master (note that the branch is production):

      cd dmc-grails
      git pull origin master # enter your credentials
     
- do the same for texture (usually)

      cd texture
      git pull 
      
- do build for client

      cd ../client # into the dmc-grails/client directory
      ./update_bioentity.sh
      cd ../texture
      ./update_production.sh
      
- do build for server

      cd ../server # dmc-grails/server
      grails war 
      sudo service tomcat8 stop 
      rm -rf /var/lib/tomcat8/webapps/dmc*
      sudo cp build/libs/dmc-0.1.war /var/lib/tomcat8/webapps/dmc.war
      sudo service tomcat8 start 


## Production Trouble-shooting / info

The following ports should be open:

      netstat -tan | grep LISTEN
      tcp        0      0 0.0.0.0:80              0.0.0.0:*               LISTEN     
      tcp        0      0 0.0.0.0:22              0.0.0.0:*               LISTEN     
      tcp        0      0 0.0.0.0:443             0.0.0.0:*               LISTEN     
      tcp6       0      0 :::8080                 :::*                    LISTEN     
      tcp6       0      0 :::80                   :::*                    LISTEN     
      tcp6       0      0 :::7473                 :::*                    LISTEN     
      tcp6       0      0 :::7474                 :::*                    LISTEN     
      tcp6       0      0 :::21                   :::*                    LISTEN     
      tcp6       0      0 :::22                   :::*                    LISTEN     
      tcp6       0      0 127.0.0.1:8005          :::*                    LISTEN     
      tcp6       0      0 :::7687                 :::*                    LISTEN 

- 8080 is tomcat.  If absent:

      sudo service tomcat8 start 
     
- 80 is nginx.  If absent:

      sudo service nginx start 
      
- 7473, 7474, 7687 are neo4j.  If absent:

      sudo service neo4j start  
      
or maybe

      neo4j start 
      

To login to the neo4j server:

      cypher-shell

and then: `neo4j/LAT8gjThKJ26TR`

Use ```neo4j-admin dump``` and ```neo4j-admin load``` to dump and load. 


- Is the server stuck / thrashing:

      top # if it shows as stuck at 100% that is okay, unless it is forever (>2 hours)
      
To restart both the tomcat and neo4j server:

      sudo service tomcat8 stop 
      sudo service neo4j stop 

Confirm that the servers are both down as above or use `ps -ef | grep neo4j` and `ps -ef | grep catalina`. 

Then restart in this order:
  
      sudo service neo4j start
      sudo service tomcat8 start
      
      
If all else fails you may need to reboot the whole thing (which will be unusual):

      sudo init 6
      
When things come back up, make sure all of the services have started as above.  If not, follow the instructions on how to restart them.      

# Unbuntu 16 production deployment 
Install:
- git 
- java 8+  (open-jdk-8 is fine) 
- apt-get install
- tomcat 8 (apt-get install tomcat8)
- neo4j 

To run neo4j in docker:

    docker run \                  
        --publish=7474:7474 --publish=7687:7687 \
        --volume=$HOME/neo4j/data:/data \
        --volume=$HOME/neo4j/logs:/logs \
        neo4j:3.1



GIT CLONE:
- git clone https://github.com/InSilicoLabs/dmc-grails
- git clone https://github.com/InSilicoLabs/GSA-Pipeline
- git clone https://github.com/InSilicoLabs/jobs-database


NEO4J INSTALLATION: 
- wget -O - https://debian.neo4j.org/neotechnology.gpg.key | sudo apt-key add -
- echo 'deb http://debian.neo4j.org/repo stable/' | sudo tee -a /etc/apt/sources.list.d/neo4j.list
- sudo apt-get update
- sudo apt-get install neo4j=3.1.4
- neo4j start  (or configure globall)

BUILD FOR TOMCAT
- cd dmc-grails
- ./gradlew deploy 
- cp dmc-grails-XXX.war /usr/local/tomcat/webapps/dmc.war 
- Start Tomcat


# Getting started (Dev Environment)

- Install http://sdkman.io/
- Install tomcat 8.0 (brew install tomcat via homebrew)
- sdk install grails 3.2.11  ```# set as default```
- export SDKMAN_DIR="$HOME/.sdkman"
- [[ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]] && source "$HOME/.sdkman/bin/sdkman-init.sh"
- git clone https://github.com/InSilicoLabs/dmc-grails
- cd server
- start up docker
-     docker run \
        --publish=7474:7474 --publish=7687:7687 \
        --volume=$HOME/neo4j/data:/data \
        --volume=$HOME/neo4j/logs:/logs \
        neo4j:3.1
- from the neo4j browser, change password to appropriate app pswd in application.yml
- grails 
  grails> run-app
- export NVM_DIR="$HOME/.nvm"
- [ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"
- install npm
- npm install -g angular-cli
- cd client/
- npm install
- npm start
- git submodule update --init --recursive
- git checkout dmc_v1
- npm install
- curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.33.5/install.sh | bash
- export NVM_DIR="$HOME/.nvm"
- [ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"
- export NVM_DIR="$HOME/.nvm" 
- [ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"
- nvm install node
- npm install
- npm start

- go to: http://localhost:8080/publication
[]
- go to: http://localhost:4200/#/index?admin=true
server/src/test/resources/lexica/inputs/ has test files to load
- load up test publications
- add keyword set
- add lexicon test
- GSA-pipeline repo has pub xml to load.

## Reference Documentation
- [Grails doc](http://docs.grails.org/latest/guide/single.html)

## Matching docs

From https://github.com/InSilicoLabs/dmc-grails/issues/157

- https://dev.insilico.link/#/index?pub=77106 

Has a DOI ending in 200592:

- http://textpresso-dev.cacr.caltech.edu/gsa/worm/first_pass_entity_link_tables/200592.html 
- http://textpresso-dev.cacr.caltech.edu/cgi-bin/gsa/worm/edit.pl?docid=200592



### TODO  (move to GH / Asana later)
- update schema based on Todd's doc
- add cvs uploader 
- add page view (with screenshots)
- implement Swagger with plugin (for 'public' webservices)

### Angular2 with Grails profiles and Texture

#### Angular2 
Use this doc to get the build going: 

- https://github.com/grails-profiles/angular

Install most recent stable node and npm and ```angular-cli```.

    npm install -g angular-cli 

#### Texture

Texture is forked here:  https://github.com/InSilicoLabs/texture

     

#### Putting it all together

Run them separately for best results:

    # terminal 1, server port 8080
    cd server 
    ./grailsw run-app
    
    # terminal 2, ng port 4200
    cd client 
    ng serve
    
    # terminal 3, ng port 5555
    cd texture 
    npm run start 
    

All can be accessed at http://localhost:port/


## Deploy production
  
#### Server

    cd server 
    ./grailsw war
    sudo systemctl stop tomcat8
    sudo rm -rf /var/lib/tomcat8/webapps/dmc
    sudo cp build/libs/dmc-0.1.war /var/lib/www/dmc.war
	sudo systemctl start tomcat8

Note: I'm currently using run-app with screen instead of the proper war file.
    
#### NG2 Client

    cd client 
    ng build --prod 
    sudo rm -rf /var/www/html/*
    sudo cp -r dist/* /var/www/html/

#### Texture Client

If not available when you clone (would have to clone recursive) do:

    git submodule update --recursive --remote
    git pull --recurse-submodules
    
The build as normal:

    cd texture
    rm -rf dist
    npm install 
    node make 
    # ctrl-c (will have built it)
    sudo rm -rf /var/www/html/texture/
    sudo cp -r dist /var/www/html/texture
    
## Neo4j Production Notes 
   
In `neo4j.conf` you have to set the address as follows:

#    dbms.connectors.default_advertised_address=dev.insilicodesigns.com
	dbms.connectors.default_advertised_address=dev.insilico.link
    dbms.memory.heap.max_size=8g
    
 and I think these as well:
       
    # With default configuration Neo4j only accepts local connections.
    # To accept non-local connections, uncomment this line:
    dbms.connectors.default_listen_address=0.0.0.0 
    
These might already be set by default, but if not, they need to be exposed:


    # Bolt connector
    dbms.connector.bolt.enabled=true
    #dbms.connector.bolt.tls_level=OPTIONAL
    dbms.connector.bolt.listen_address=:7687

    # HTTP Connector. There must be exactly one HTTP connector.
    dbms.connector.http.enabled=true
    dbms.connector.http.listen_address=:7474
   
Set the ulimit on some instances in the startup neo4j script:

    ulimit -n 100000 # default is 1024

### Clean out database

    MATCH (n:Lexicon)-[s:LEXICONSOURCE]-(p:LexiconSource) return n limit 25 ;
    MATCH (n:Lexicon)-[s:LEXICONSOURCE]-(p:LexiconSource) delete s  ;
    
    MATCH (c:LexicaCapture)-[r:LEXICA]-(n:Lexicon) return c limit 5 ;
    MATCH (n:LexicaCapture)-[r:SOURCE]-(q:LexiconSource) delete r,n;
    match (s:Lexicon)-[r:LEXICA]-(c:LexicaCapture) delete s,r,c;
    
    MATCH (n:Lexicon)-[r:LEXICA]-(k:KeyWord) return n limit 5 ;
    MATCH (n:Lexicon)-[r:LEXICA]-(k:KeyWord)  delete r ;
    
    MATCH (n:Lexicon) delete n ;
    
    MATCH (n:KeyWord)-[r:KEYWORDSET]-(q:KeyWordSet) return n limit 25 ;
    MATCH (n:KeyWord)-[r:KEYWORDSET]-(q:KeyWordSet) delete r;
    MATCH (n:KeyWord) delete n ;
    MATCH (n:KeyWordSet)-[r:SOURCES]-(s:LexiconSource) return n ;
    MATCH (n:KeyWordSet)-[r:SOURCES]-(s:LexiconSource) delete n,r ;
    
    MATCH (n:KeyWord) delete n ;
    

    // delete markups
    MATCH (n:Markup)-[r:PUBLICATION]-(p:Publication) delete r ;
    MATCH (n:Markup)-[f:FINALLEXICON]-(l:Lexicon) delete n,f  
    
    
### Other Notes

Example configuration for Grails / Neo4j tests: https://github.com/grails/gorm-neo4j/tree/master/examples

### Other Important Set

    MATCH (kws:KeyWordSet)--(k:KeyWord)--(l:Lexicon)--(s:LexiconSource) where s.source='Source 3'  RETURN kws,k,l,s limit 25
    
### License
    CC BY-NC 4.0
