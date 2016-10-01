var network;
var graph;
var isis_data;
var selected_node_id;

var expand_form = $('#expand_form');
var cluster_form = $('#cluster_form');

expand_form.hide();
cluster_form.hide();


function loadJSON(url, callback) {

    var xobj = new XMLHttpRequest();
    xobj.overrideMimeType('application/json');
    xobj.open('GET', url, true);
    xobj.onreadystatechange = function () {
        if (xobj.readyState == 4) {
            if (xobj.status == '200') {
                callback(xobj.responseText);
            }
        }
    };
    xobj.send(null);
}


function highlight_devices() {
    var dev_input = $("#device_input");

    var id = dev_input.val();
    dev_input.text(id);
    var newColor = "red";
    var normalColor = "#D2E5FF";

    var arrayLength = isis_data['nodes'].length;
    for (var i = 0; i < arrayLength; i++) {
        if (isis_data['nodes'][i]['id'] == id) {
            graph.nodes.update([{id: id, color: {background: newColor}}]);
        } else {
            var other_id = isis_data['nodes'][i]['id'];
            graph.nodes.update([{id: other_id, color: {background: normalColor}}]);
        }
    }
    return false;
}


function clusterByHub() {
    var colors = ["orange", "lime", "violet", "pink", "white"];

    var nodes_num = isis_data['nodes'].length;
    var hubs = [];
    for (var i = 0; i < nodes_num; i++) {
        var hub = isis_data['nodes'][i]['group'];
        if (hub && hubs.indexOf(hub) == -1) {
            hubs.push(hub)
        }
    }

    for (var j = 0; j < hubs.length; j++) {
        var clusterOptionsByData = {
            joinCondition: function (childOptions) {
                return childOptions.group == hubs[j];
            },
            clusterNodeProperties: {
                id: 'cluster:' + hubs[j],
                title: hubs[j],
                borderWidth: 3,
                label: hubs[j],
                color: {background: colors[j]}
            }

        };

        network.cluster(clusterOptionsByData);
        network.stabilize();
    }
}


var device_bh = new Bloodhound({
    datumTokenizer: Bloodhound.tokenizers.obj.whitespace('urn'),
    queryTokenizer: Bloodhound.tokenizers.whitespace,
    limit: 10,

    prefetch: {
        cache: false,
        url: '/info/devices',
        filter: function (list) {
            return $.map(list, function (urn) {
                return {urn: urn};
            });
        }
    }
});


function devicesWithDefault(q, sync) {
    if (q === '') {
        sync(device_bh.index.all());
    } else {
        device_bh.search(q, sync);
    }
}


// otherwise typeahead doesn't work
$(document).ready(function () {
    device_bh.initialize();

    var th_opts = {
        hint: false,
        highlight: true,
        minLength: 0
    };

    $('#device_input').typeahead(th_opts, {
        name: 'devices',
        displayKey: 'urn',
        source: devicesWithDefault,
        templates: {
            suggestion: function (data) { // data is an object as returned by suggestion engine
                return '<div class="tt-suggest-page">' + data.urn + '</div>';
            }
        }
    });

    $('#device_form').on('submit', function (e) {
        e.preventDefault();
        highlight_devices();
    });

    $('#ifce_form').on('submit', function (e) {
        e.preventDefault();
        highlight_ifces();
    });

    $('#circuit_form').on('submit', function (e) {
        e.preventDefault();
        highlight_circuits();
    });

    $('#cluster_form').on('submit', function (e) {
        e.preventDefault();
        clusterByHub();
        cluster_form.hide();
        if (selected_node_id != null) {
            expand_form.show();

        }
    });

    $('#expand_form').on('submit', function (e) {
        e.preventDefault();
        network.openCluster(selected_node_id);
        cluster_form.show();
        expand_form.hide();
        network.stabilize();
    });

    loadJSON("/graphs/isis", function (response) {
        // Parse JSON string into object
        isis_data = JSON.parse(response);
        var height = 500;
        var options = {
            height: height + 'px',
            interaction: {
                zoomView: true,
                dragView: true,
                hideEdgesOnDrag: false
            },
            physics: {
                stabilization: true
            },
            nodes: {
                shape: 'dot'
            },
            edges: {}
        };

        // create an array with nodes
        var nodes = new vis.DataSet(isis_data['nodes']);
        var edges = new vis.DataSet(isis_data['edges']);

        // create a network
        var container = document.getElementById('network_viz');
        graph = {
            nodes: nodes,
            edges: edges
        };


        network = new vis.Network(container, graph, options);

        network.on('dragEnd', function (params) {
            for (var i = 0; i < params.nodes.length; i++) {
                var nodeId = params.nodes[i];
                console.log("dragEnd" + nodeId);

                if (network.isCluster(nodeId) == true) {
                    console.log("dragEnd: cluster " + nodeId);
                    network.clustering.updateClusteredNode(nodeId, {fixed: {x: true, y: true}});
                } else {
                    console.log("dragEnd: plain " + nodeId);
                    graph.nodes.update({id: nodeId, fixed: {x: true, y: true}});
                }
            }
        });

        network.on('dragStart', function (params) {

            for (var i = 0; i < params.nodes.length; i++) {
                var nodeId = params.nodes[i];
                console.log("dragStart " + nodeId);

                if (network.isCluster(nodeId) == true) {
                    console.log("dragStart: cluster " + nodeId);
                    network.clustering.updateClusteredNode(nodeId, {fixed: {x: false, y: false}});

                } else {
                    console.log("dragStart: plain " + nodeId);
                    graph.nodes.update({id: nodeId, fixed: {x: false, y: false}});

                }
            }
        });

        network.on("click", function (params) {
            var clickedNode = false;
            selected_node_id = null;
            for (var i = 0; i < params.nodes.length; i++) {

                var positions = network.getPositions()
                $("#position").JSONView(positions, {collapsed: true});

                clickedNode = true;
                var nodeId = params.nodes[i];
                console.log("node selected " + nodeId);

                if (network.isCluster(nodeId) == true) {
                    expand_form.show();
                    selected_node_id = nodeId;
                    $('selected_hub').text(nodeId);
                } else {
                    expand_form.hide();
                    selected_node_id = null;

                    loadJSON("/node_info/" + nodeId, function (response) {
                        console.log("node info: " + nodeId);
                        var node_info = JSON.parse(response);
                        show_info_card(node_info);
                    });
                }
            }

            if (!clickedNode) {
                expand_form.hide();
                for (var i = 0; i < params.edges.length; i++) {
                    var edgeId = params.edges[i];
                    edgeId = network.clustering.getBaseEdge(edgeId);
                    console.log("edge selected: " + edgeId);

                    loadJSON("/edge_info/" + edgeId, function (response) {
                        var edge_info = JSON.parse(response);
                        if (edge_info.display) {
                            show_info_card(edge_info);
                            console.log("edge displayed: " + edgeId);
                        } else {
                            $("#info_card").hide();
                            console.log("edge hidden: " + edgeId);

                        }
                    });
                }
            }

        });

        clusterByHub();

    });

});




function show_info_card(info_card) {
    $("#info_card").show();
    $("#info_card").JSONView(info_card, {collapsed: true});
}
