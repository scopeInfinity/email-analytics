<?php 
require_once 'vendor/autoload.php';
use GraphAware\Neo4j\Client\ClientBuilder;


//Temperary
// Currently for only one root Email
if(!isset($_POST['email']))
  $_POST['email'] = 'gagan1kumar.cs@gmail.com';

//Operation To be done
if(!isset($_GET['op']))
  $_GET['op'] = 'graph';


//neo4j client
$client = ClientBuilder::create()
    ->addConnection('bolt', 'bolt://localhost:7687')
    ->build();

switch ($_GET['op']) {
  case 'graph':
      echo fetchGraph($client);
    break;
  case 'dashboard':
      $countMap = array('User' => countUsers($client),
                        'Email' => countEmails($client),
                        'Reply' => countReply($client) );
      $dashboard  = array('count' => $countMap, );
      //Wrap of all Details and push to DashBoard
      echo json_encode($dashboard);
    break;
  default:
      echo "Invalid Operation";
      exit(-1);
    break;
}
exit(0);



function countUsers($client) {
  return fetchCount($client,"User");
}
function countEmails($client) {
  return fetchCount($client,"Email");
}
function countReply($client) {
  return fetchCount($client,"Reply");
}


function fetchCount($client, $type) {
  $query = "MATCH (n:".$type.") RETURN count(n) as count";
  $result = $client->run($query);
  return ($result->firstRecord()->get('count'));

}

//Get the Required Graph
function fetchGraph($client) {
    $noOfLink = 2;
    $query = "MATCH p=(u:User )-[r0:Link]->(e:Email)-[r1:Link]->(u2:User {Email:'".$_POST['email']."'}) WHERE u<>u2   RETURN r0,r1,p LIMIT 15";
    $result = $client->run($query);

    // Final Collection of Nodes and Edges
    $Nodes = array();
    $Edges = array();

    foreach ($result->getRecords() as $record) {

      $nodes = $record->get('p')->nodes();
      $relation  = array();
      for ($i=0; $i < $noOfLink; $i++) { 
        $relation[] = $record->values()[$i];
      }
      
      //For each Node Insert Details with their id as key
      foreach ($nodes as $id => $node ) { 
        $id = $node->identity();
        $data = $node->values();
        

        //Email+Reply As Label ????
        $label = $node->labels()[0];
        if($label=='User')
          $viewLabel = $node->value('Email');
        else
          $viewLabel = "Email";
      
        
        $Nodes[$id] = array('type'=>$label,'label'=>$viewLabel,'data' => $data);

      }

      //For each edges Put Content into the Matrix
      foreach ($relation as $r) {
        //In Matrix Representation, Because it may have repetition
        $Edges[$r->startNodeIdentity()][$r->endNodeIdentity()] = $r->values();

      }
     
    }

    //Finally returing Output in JSON Format
    $data = array('nodes' => $Nodes,'edges' => $Edges );
    return json_encode($data,JSON_PRETTY_PRINT);
}