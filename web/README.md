## Web-Interface
Web-Interface for grabbing our content of emails analysis from a Graph Database(neo4j).

### Requirements
* PHP >= 5.6
* A Neo4j database (minimum version 2.2.6)

### Installation and Configurations
* Follow [https://getcomposer.org/download/](https://getcomposer.org/download/) for Downloading composer.
* Adding __neo4j-php-client__ library to composer dependencies. [\[Source\]](https://github.com/graphaware/neo4j-php-client)
```
composer require graphaware/neo4j-php-client
```
* Follow [README](../README.md) of root `Email-Analytics` for running it properly.

### TestRun
* Goto `/path/to/email-analytics/script`
* Use `sh GenerateJar.sh` for Generating jar file. If it fails, please go back to root [README](../README.md).
* Use `sh RunExample.sh` to Execute the Example and follow instruction for adding data to Neo4j database.
* Move to `/path/to/email-analytics/web` and execute `php -S 0.0.0.0:8080` for hosting the Web Interface.
* Finally open `http://localhost:8080/` in your web browser for testing it.
    Note : Some libraries are used directly from public url, So internet access may be required for the complete functioning.