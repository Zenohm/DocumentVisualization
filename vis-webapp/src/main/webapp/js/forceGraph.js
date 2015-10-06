/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Chris Bellis, Chris Perry
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Created by Chris on 7/14/2015.
 */

// TODO: This code needs some documentation
// TODO: This code also needs a lot of refactoring, we should pull out some of the functionality and make it into nicer funcitons
var myLoc = document.getElementById("main");

var width = myLoc.clientWidth - 5,
    height = myLoc.clientHeight - 5;
r = 0;

var clusterPadding = 6, // separation between different-color nodes
    maxRadius = 12,
    padding = 1.5;

var color = d3.scale.category20();

// Create the force layout
var force = d3.layout.force()
    .charge(-150)
    .linkDistance(50)
    .size([width, height]);

// Create the actual display
var svg = d3.select("#main").append("svg")
    .attr("width", width)
    .attr("height", height);

function generateGraph() {
    svg.selectAll("*").remove();
    d3.json("testData.json", dataHandler);
}

/**
 * This function handles data.
 * @param error if an error is thrown
 * @param graph the data from the graph
 */
function dataHandler(error, graph) {
    if (error) throw error;

    force.nodes(graph.nodes)
        .links(graph.links)
        .start();

    var link = svg.selectAll(".link")
        .data(graph.links)
        .enter().append("line")
        .attr("class", "link")
        .style("stroke-width", function (d) {
            return Math.sqrt(d.value);
        });

    var node = svg.selectAll(".node")
        .data(graph.nodes)
        .enter().append("circle")
        .attr("class", "node")
        .attr("r", 5)
        .style("fill", function (d) {
            return color(d.group);
        })
        .call(force.drag);

    d3.selectAll("circle.node")
        .on("click", function () {
            d3.select(this).attr("r", 25);
            force.on("tick", onTick);
        })
        .on("dblclick", function () {
            d3.select(this).attr("r", 5);
        });

    node.append("title")
        .text(function (d) {
            return d.name;
        });

    force.on("tick", onTick);
    d3.select(window).on("resize", resize);

    /**
     * This function is applied on resize.
     */
    function resize(e) {
        var myLoc = document.getElementById("main");

        var width = myLoc.clientWidth - 5,
            height = myLoc.clientHeight - 5;

        svg.attr('width', width);
        svg.attr('height', height);
        force.size([width, height]).resume();

        // We have to reapply the collision and the movement functions (with the new width)
        force.on("tick", onTick);
    }

    /**
     * Function that is called on every tick
     */
    function onTick() {
        link.attr("x1", function (d) {
            return d.source.x;
        })
            .attr("y1", function (d) {
                return d.source.y;
            })
            .attr("x2", function (d) {
                return d.target.x;
            })
            .attr("y2", function (d) {
                return d.target.y;
            });

        node.each(collide(.5))
            .attr("cx", function (d) {
                return d.x = Math.max(r, Math.min(width - r, d.x));
            })
            .attr("cy", function (d) {
                return d.y = Math.max(r, Math.min(height - r, d.y));
            });
    }

    /**
     * This function is used to collide every node.
     * @param alpha
     * @returns {Function}
     */
    function collide(alpha) {
        var quadtree = d3.geom.quadtree(graph.nodes);
        return function (d) {
            var r = d.radius + maxRadius + Math.max(padding, clusterPadding),
                nx1 = d.x - r,
                nx2 = d.x + r,
                ny1 = d.y - r,
                ny2 = d.y + r;
            quadtree.visit(function (quad, x1, y1, x2, y2) {
                if (quad.point && (quad.point !== d)) {
                    var x = d.x - quad.point.x,
                        y = d.y - quad.point.y,
                        l = Math.sqrt(x * x + y * y),
                        r = d.radius + quad.point.radius + (d.cluster === quad.point.cluster ? padding : clusterPadding);
                    if (l < r) {
                        l = (l - r) / l * alpha;
                        d.x -= x *= l;
                        d.y -= y *= l;
                        quad.point.x += x;
                        quad.point.y += y;
                    }
                }
                return x1 > nx2 || x2 < nx1 || y1 > ny2 || y2 < ny1;
            });
        };
    }
}

