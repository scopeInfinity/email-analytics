<?php 
require_once 'vendor/autoload.php';
use GraphAware\Neo4j\Client\ClientBuilder;


//Temperary

//Operation To be done
if(!isset($_GET['op']))
  $_GET['op'] = 'graph';


//neo4j client
$client = ClientBuilder::create()
    ->addConnection('bolt', 'bolt://localhost:7687')
    ->build();

switch ($_GET['op']) {
  case 'graph':
      if(isset($_POST['email']))
        echo fetchGraph($client);
      else
        echo "Please Provide EmailID";
    break;
  case 'users':
      if(isset($_POST['email']))
        echo fetchGraphUsers($client,$_POST['email']);
      else
        echo "Please Provide EmailID";
    break;
  case 'dashboard':
      $countMap = array('User' => countUsers($client),
                        'Email' => countEmails($client),
                        'Reply' => countReply($client) );
      $dashboard  = array('count' => $countMap, );
      //Wrap of all Details and push to DashBoard
      echo json_encode($dashboard);
    break;
  case 'avgSentiments':
      if(!isset($_GET['from'])) 
        $_GET['from'] = null;
      if(!isset($_GET['to'])) 
        $_GET['to'] = null;

      echo fetchSentiment($client,$_GET['from'], $_GET['to']);
      break;
  case 'userList':
      echo fetchUsers($client);
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

//Get the Required Graph All
function fetchGraph($client, $email) {
    $noOfLink = 2;

    $query = "MATCH p=(u:User )-[r0:Link]->(e:Email)-[r1:Link]->(u2:User {Email:'".$email."'}) WHERE u<>u2   RETURN r0,r1,p LIMIT 15";
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

//Get the Required Graph Users
function fetchGraphUsers($client,$email) {
    $query = "MATCH p=(u:User )-[r0:Link]->(e:Email)-[r1:Link]->(u2:User {Email:'".$email."'}) WHERE u<>u2   RETURN u,u2,e ";
    $result = $client->run($query);

    // Final Collection of Nodes and Edges
    $Nodes = array();
    $Edges = array();

    foreach ($result->getRecords() as $record) {
      $nodes = array();
      $nodes[] = $record->get('u');
      $nodes[] = $record->get('u2');
      $emailData = $record->get('e')->values();
      $ids = array();
      
      //For each Node Insert Details with their id as key
      foreach ($nodes as  $node ) { 
        $id = $node->identity();
        $ids[] = $id;
        $data = $node->values();
        

        //Email+Reply As Label ????
        $label = $node->labels()[0];
        if($label=='User')
          $viewLabel = $node->value('Email');
        else
          $viewLabel = "Email";
      
        
        $Nodes[$id] = array('type'=>$label,'label'=>$viewLabel,'data' => $data);

      }

      if(array_key_exists($ids[0],$Edges) && array_key_exists($ids[1],$Edges[$ids[0]]))
        $Edges[$ids[0]][$ids[1]]['sentiments'][] = $emailData['Sentiment'];
      else
        $Edges[$ids[0]][$ids[1]]  = array('sentiments' => array($emailData['Sentiment']) );
    }

    //Finally returing Output in JSON Format
    $data = array('nodes' => $Nodes,'edges' => $Edges );
    return json_encode($data,JSON_PRETTY_PRINT);
}

//Get the Required Graph Users
function fetchSentiment($client,$from,$to) {
    
    $query = "MATCH p=(u:User )-[r0:Link]->(e:Email)-[r1:Link]->(u2:User) WHERE u<>u2 ";
    if(!is_null($from))
      //$query = $query . " and id(u)={$from}";
      $query = $query . " and u.Email =~ '.*".$from.".*' ";

    if(!is_null($to))
      $query = $query . " and u2.Email =~ '.*".$to.".*' ";
      //$query = $query . " and id(u2)={$to}";

    $query = $query." RETURN avg(e.Sentiment) as avgSentiments ";
    $result = $client->run($query);
    //print_r($result);
    $record = $result->firstRecord();
    $value = $record->get('avgSentiments');
    if($value == NULL)
      return "NULL";
    return $value;
}

//Get the Required Graph Users
function fetchUsers($client) {
    
    $query = "Match (n:User) Return n.Email;";
    $result = $client->run($query);
    $record = $result->firstRecord();
    $users = array();
    foreach ($result->getRecords() as $record) {
      $users[] = $record->values()[0];
    }
    
    return json_encode($users);
}