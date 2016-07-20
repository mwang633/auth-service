# auth-service

An example restful service based on spray and slick
 
##pull submodules
 `git submodule update --init --recursive`

##compile
`sbt compile`

##start the service
`sbt run`

if you run
`curl http://localhost:8084/Echo/Test`

you should get `Test` back in the response


##continuous development
This project has sbt-revolver plugin, which allows sbt to detect code changes and automatically rebuild and restart the server

`sbt` then `~ re-start` to start

