# Assignment Scalable Web

 Provide 2 http endpoints that accepts JSON base64 encoded binary data on both
 endpoints
 
    <host>/v1/diff/<ID>/left and <host>/v1/diff/<ID>/right
 
 The provided data needs to be diff-ed and the results shall be available on a third end
 point
  
    <host>/v1/diff/<ID>
 
 The results shall provide the following info in JSON format
 * If equal return that
 * If not of equal size just return that
 * If of same size provide insight in where the diffs are, actual diffs are not needed.
 § So mainly offsets + length in the data

  # Solution


  Play Framework is used for a fully asynchronous model built on top of Akka., a high-productivity Scala application that integrates components and APIs.
   # Depecdencies
      "mysql" % "mysql-connector-java" % "5.1.37" //to save left and right data
      "org.playframework.anorm" %% "anorm" % "2.6.2" //data access layer that uses plain SQL
      "io.swagger" % "swagger-core" % "1.5.18" // API documentation

      "org.specs2" % "specs2-core_2.11" % "3.6.6" % "test"
      "org.specs2" % "specs2-junit_2.11" % "3.6.6" % "test"
      "org.specs2" % "specs2-mock_2.11" % "3.6.6" % "test"
      "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
      "org.scalatest" %% "scalatest" % "2.2.6" % Test
      "org.mockito" % "mockito-core" % "1.9.5" % Test

   # How to run
   * Clone the JsonDiff repository to your computer using the following command.
      
         git clone https://github.com/smr-iz/jsondiff.git
   
   * Create a new database (e.g. `json_diff`) on your local MySQL instance. You don’t need to create the tables on the database. Because running app automatically creates them by executing the SQL evolutions in the project.Don't forget giving DDL and DML permissions to the mysql user.
    
   * Override values on application.sample.conf with created database credentials.
    
    db.default.url = """jdbc:mysql://localhost/json_diff?characterEncoding=UTF-8""" 
    db.default.username="<username>"
    db.default.password="<password>"

   * Preferably, first run followng script to read application.conf. And then type 'run' on Json_Diff console.
     
    activator -mem 5120 -J-Xss4M -jvm-debug 9997 -Dhttp.port=8080 -Dhttps.port=8081 -Dconfig.file=conf/application.sample.conf
   
   * For running tests, typing 'test' on Json_Diff console will be enough.

    API: POST       /v1/diff/:id/left       controllers.JsonController.setLeft(id: String)
         POST       /v1/diff/:id/right      controllers.JsonController.setRight(id: String)
         GET        /v1/diff/:id            controllers.JsonController.get(id: String)

    Here is an example curl to test the api which runs on port: 8080
       curl -d 'ewogICAgInRlc3QiOiB7CiAgICAgICAgInRpdGxlIjogImV4YW1wbGUgdGl0bGUiCiAgICB9Cn0=' -H "Content-Type: text/plain" -X POST http://localhost:8080/v1/diff/394b000a-fd43-11e8-8eb2-f2801f1b9fd1/right
       curl -d 'ewogICAgInRlc3QiOiB7CiAgICAgICAgInRpdGxlIjogImV4YW1wbGUgdGl0bGUiCiAgICB9Cn0=' -H "Content-Type: text/plain" -X POST http://localhost:8080/v1/diff/394b000a-fd43-11e8-8eb2-f2801f1b9fd1/left
       curl -X GET http://localhost:8080/v1/diff/394b000a-fd43-11e8-8eb2-f2801f1b9fd1