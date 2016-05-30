//
//  view.js
//
//  Used a project template from arbor.js
//
var imgEmail = document.getElementById("email");
var imgUser = document.getElementById("user");
var nodeEmailSize = 50;
var nodeUserSize = 50;
var nodeAvgSize = 50;
    
(function($){

  var Renderer = function(canvas){
    var canvas = $(canvas).get(0)
    var ctx = canvas.getContext("2d");
    var particleSystem

    var that = {
      init:function(system){
        //
        // the particle system will call the init function once, right before the
        // first frame is to be drawn. it's a good place to set up the canvas and
        // to pass the canvas size to the particle system
        //
        // save a reference to the particle system for use in the .redraw() loop
        particleSystem = system

        // inform the system of the screen dimensions so it can map coords for us.
        // if the canvas is ever resized, screenSize should be called again with
        // the new dimensions


        particleSystem.screenSize(
                     document.getElementById('viewport').clientWidth,
                     document.getElementById('viewport').clientHeight); 
        particleSystem.screenPadding(20) 
        // set up some event handlers to allow for node-dragging
        that.initMouseHandling()
      },
      
      redraw:function(){
        // 
        // redraw will be called repeatedly during the run whenever the node positions
        // change. the new positions for the nodes can be accessed by looking at the
        // .p attribute of a given node. however the p.x & p.y values are in the coordinates
        // of the particle system rather than the screen. you can either map them to
        // the screen yourself, or use the convenience iterators .eachNode (and .eachEdge)
        // which allow you to step through the actual node objects but also pass an
        // x,y point in the screen's coordinate system
        // 
        ctx.fillStyle = "white"
        ctx.fillRect(0,0, canvas.width, canvas.height)
        
        particleSystem.eachEdge(function(edge, pt1, pt2){
          // edge: {source:Node, target:Node, length:#, data:{}}
          // pt1:  {x:#, y:#}  source position in screen coords
          // pt2:  {x:#, y:#}  target position in screen coords

          // draw a line from pt1 to pt2
          if (edge.data.properties["Relation"]=='TO')
            ctx.strokeStyle = "rgba(0,255,0, .666)"
          else if (edge.data.properties["Relation"]=='FROM')
            ctx.strokeStyle = "rgba(0,0,255, .666)"

          ctx.lineWidth = 1
          ctx.beginPath()
          canvas_arrow(ctx,pt1.x,pt1.y,pt2.x,pt2.y);
          // ctx.moveTo(pt1.x, pt1.y)
          // ctx.lineTo(pt2.x, pt2.y)
          ctx.stroke()
        })

        particleSystem.eachNode(function(node, pt){
          // node: {mass:#, p:{x,y}, name:"", data:{}}
          // pt:   {x:#, y:#}  node position in screen coords

          // draw a rectangle centered at pt
          var label = node.data.label;
          var size, img = null;
          if (node.data.type == 'User')
          {
            img = imgUser;  size = nodeUserSize;
          }
          else if (node.data.type == 'Email')
          {
            img = imgEmail;  size = nodeEmailSize;
            label = node.data.properties['Subject'];
            if(label.length>20)
              label = label.substring(0,20)+"...";
          }
          if(img!==null)
            ctx.drawImage(img,pt.x-size/2,pt.y-size/2,size,size);
          else
          {
            var w = 10
            ctx.fillStyle = "black"
            ctx.fillRect(pt.x-w/2, pt.y-w/2, w,w)
          }

          //Draw Label
          if(label && label!="") {
            textDetails = ctx.measureText(label);
            var width = textDetails.width+5;
            var height = 16;//ctx.font.size;//textDetails.height+3;

            ctx.globalAlpha=0.7;
            ctx.fillStyle = "white"  
            ctx.fillRect(pt.x-width/2,pt.y-height/2+size*0.4,width,height);
            ctx.globalAlpha=1;
            ctx.fillStyle = "black"  
            ctx.fillText(label,pt.x-width/2+2,pt.y+size*0.4+2 );
            

          }
        })    			
      },
      
      initMouseHandling:function(){
        // no-nonsense drag and drop (thanks springy.js)
        var dragged = null;

        // set up a handler object that will initially listen for mousedowns then
        // for moves and mouseups while dragging
        var handler = {
          clicked:function(e){
            var pos = $(canvas).offset();
            _mouseP = arbor.Point(e.pageX-pos.left, e.pageY-pos.top)
            dragged = particleSystem.nearest(_mouseP);

            if (dragged && dragged.node !== null){
              // while we're dragging, don't let physics move the node
              dragged.node.fixed = true
            }

            if (dragged.node !== null) {
              showProperties(dragged.node.data.properties);
            }

            $(canvas).bind('mousemove', handler.dragged)
            $(window).bind('mouseup', handler.dropped)

            return false
          },
          dragged:function(e){
            var pos = $(canvas).offset();
            var s = arbor.Point(e.pageX-pos.left, e.pageY-pos.top)

            if (dragged && dragged.node !== null){
              var p = particleSystem.fromScreen(s)
              dragged.node.p = p
            }

            return false
          },

          dropped:function(e){
            if (dragged===null || dragged.node===undefined) return
            if (dragged.node !== null) dragged.node.fixed = false
            dragged.node.tempMass = 1000
            dragged = null
            $(canvas).unbind('mousemove', handler.dragged)
            $(window).unbind('mouseup', handler.dropped)
            _mouseP = null
            return false
          }
        }
        
        // start listening
        $(canvas).mousedown(handler.clicked);

      },
      
    }
    return that
  }    

   $(document).ready(function(){

    // Calling `data.php` for obtaning Graph
    jQuery.ajax({
      url: 'data.php?op=graph',
      success: function(data) {
                
                var sys = arbor.ParticleSystem(1000, 600, 0.7); // create the system with sensible repulsion/stiffness/friction
                sys.parameters({gravity:true}); // use center-gravity to make the graph settle nicely (ymmv)
                sys.renderer = Renderer("#viewport"); // our newly created renderer will have its .init() method called shortly by sys...


                var pushedData = jQuery.parseJSON(data);
                $.each(pushedData['nodes'], function(i, serverData)
                {
                    var mass = (serverData['type']=='User')?0.7:0.2;
                    sys.addNode(i, {properties: serverData['data'],
                                    type: serverData['type'],
                                    label: serverData['label'],
                                    mass:mass});
                });
                $.each(pushedData['edges'], function(i, destinations)
                {
                    $.each(destinations, function(j, property)
                    {
                        sys.addEdge(i,j, {
                          properties: property
                        } );
                    }) 
                });
     
      },
      async:false
    });
  });

})(this.jQuery)