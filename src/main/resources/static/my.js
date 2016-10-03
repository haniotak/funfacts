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


function highlight_network(hl_node_ids, hl_edge_ids) {
    var newColor = "red";
    var normalNodeColor = "white";
    var normalEdgeColor = "#2B7CE9";

    var all_nodes_length = isis_data['nodes'].length;
    var all_edges_length = isis_data['edges'].length;

    var all_clusters = [];
    var clusters_to_hl = [];

    for (var i = 0; i < all_nodes_length; i++) {
        var current_node_id = isis_data['nodes'][i]['id']

        var highlightThis = false;
        if (hl_node_ids.indexOf(current_node_id) >= 0) {
            highlightThis = true;
        }

        var clusters_for_node = network.clustering.findNode(current_node_id);

        for (var j = 0; j < clusters_for_node.length - 1; j++) {
            var cluster_id = clusters_for_node[j];
            if (network.isCluster(cluster_id) == true) {
                if (!all_clusters.indexOf(cluster_id) >= 0) {
                    all_clusters.push(cluster_id);
                }
                if (highlightThis && !clusters_to_hl.indexOf(cluster_id) >= 0) {
                    clusters_to_hl.push(cluster_id);
                }
            }

        }

        if (highlightThis) {
            graph.nodes.update([{id: current_node_id, color: {background: newColor}}]);

        } else {
            graph.nodes.update([{id: current_node_id, color: {background: normalNodeColor}}]);

        }
    }

    for (var k = 0; k < all_clusters.length; k++) {
        var cluster_id = all_clusters[k];
        if (clusters_to_hl.indexOf(cluster_id) >= 0) {
            network.clustering.updateClusteredNode(cluster_id, {color: {background: newColor}});
        } else {
            network.clustering.updateClusteredNode(cluster_id, {color: {background: normalNodeColor}});
        }
    }

    for (var m = 0; m < all_edges_length; m++) {
        var current_edge_id = isis_data['edges'][m]['id'];
        var base_edge_id = network.clustering.getBaseEdge(current_edge_id);
        var clustered_edges = network.clustering.getClusteredEdges(base_edge_id);

        if (hl_edge_ids.indexOf(base_edge_id) >= 0) {
            console.log("highlighting "+base_edge_id);
            if (clustered_edges.length >= 2) {
                network.clustering.updateEdge(base_edge_id, {color : newColor})
            } else {
                graph.edges.update([{id: current_edge_id, color: newColor}]);
            }

        } else {
            if (clustered_edges.length >= 2) {
                network.clustering.updateEdge(base_edge_id, {color : normalEdgeColor})
            } else {
                graph.edges.update([{id: current_edge_id, color: normalEdgeColor}]);
            }
        }
    }

}

function highlight_devices() {
    var dev_input = $("#device_input");

    var id = dev_input.val();
    var nodeIds = [id];
    var edgeIds = [];

    highlight_network(nodeIds, edgeIds);

    return false;
}


function highlight_ifces() {
    var ifce_input = $("#ifce_input");

    var query = ifce_input.val();

    var jsonPath = "/highlight/ifces?query=" + query;
    loadJSON(jsonPath, function (response) {
        // Parse JSON string into object
        var hl_data = JSON.parse(response);
        console.log("queried " + jsonPath);
        console.log(hl_data);
        highlight_network(hl_data.nodeIds, hl_data.edgeIds);

    });

    return false;
}

function highlight_circuits() {
    var circuit_input = $("#circuit_input");

    var query = circuit_input.val();

    var jsonPath = "/highlight/circuits?query=" + query;
    loadJSON(jsonPath, function (response) {
        // Parse JSON string into object
        var hl_data = JSON.parse(response);
        console.log("queried " + jsonPath);
        console.log(hl_data);
        highlight_network(hl_data.nodeIds, hl_data.edgeIds);

    });

    return false;
}


function clusterByHub() {

    var nodes_num = isis_data['nodes'].length;
    var hubs = [];
    var positions = {
        "NASH": {
            "x": -232,
            "y": 166
        },
        "WASH": {
            "x": 310,
            "y": 243
        },
        "CHIC": {
            "x": -117,
            "y": -133
        },
        "BAY": {
            "x": -1364,
            "y": 181
        }
    };
    for (var i = 0; i < nodes_num; i++) {
        var hub = isis_data['nodes'][i]['group'];
        if (hub && hubs.indexOf(hub) == -1) {
            hubs.push(hub)
        }
    }


    for (var j = 0; j < hubs.length; j++) {
        var hub = hubs[j];
        var clusterOptionsByData = {
            joinCondition: function (childOptions) {
                return childOptions.group == hub;
            },
            clusterNodeProperties: {
                id: 'cluster:' + hub,
                title: hub,
                borderWidth: 3,
                label: hub,
                shape: "star",
                size: 40,
                fixed: {
                    x: true,
                    y: true
                },
                x: positions[hub].x,
                y: positions[hub].y,
                color: {background: "white"}
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
                shape: 'dot',
                color: {background: "white"}
            },
            groups: {
                useDefaultGroups: false
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
                    expand_form.show();
                    selected_node_id = nodeId;
                    $('selected_hub').text(nodeId);
                    $("#info_card").hide();

                } else {
                    console.log("dragEnd: plain " + nodeId);
                    graph.nodes.update({id: nodeId, fixed: {x: true, y: true}});

                    loadJSON("/node_info/" + nodeId, function (response) {
                        console.log("node info: " + nodeId);
                        var node_info = JSON.parse(response);
                        show_info_card(node_info);
                    });
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


                clickedNode = true;
                var nodeId = params.nodes[i];
                console.log("node selected " + nodeId);

                if (network.isCluster(nodeId) == true) {
                    expand_form.show();
                    selected_node_id = nodeId;
                    $('selected_hub').text(nodeId);
                    $("#info_card").hide();
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
