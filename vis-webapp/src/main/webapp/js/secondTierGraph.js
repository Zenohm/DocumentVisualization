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
 * Created by Chris on 7/18/2015.
 * This is an attempt at a complete rewrite of the code from the bottom up, so that it is more reusable and works
 * well with more dynamic data. (Actually, this is a shittily writen piece of crap, but LOLJAVASCRIPT)
 */

function forceChart() {
    // "Constant" Variables
    var LINK_SIZE = 40,
        FONT_SIZE = "16px";
    // "Class" Variables
    var width = 400,
        height = 400,
        force = 0,
        svg = 0,
        link = 0,
        node = 0,
        text = 0,
        graph = 0,
        me = 0,
        clickedNode = -1;

    // This generates the chart
    function chart(selection) {
        // Generate the chart
        // "this" is the window
        // 'd' is the data
        selection.each(function (d, i) {
            graph = d;

            me = this;
            height = this.clientHeight - 3;
            width = this.clientWidth - 3;

            for(var j = 0; j < graph.nodes.length; j++){
                if(d.nodes[j].fixed){
                    d.nodes[j].radius = d.nodes[j].size * 15;
                }else{
                    d.nodes[j].radius = d.nodes[j].size * 35;
                }

            }

            force = d3.layout.force()
                .charge(-150)
                .linkDistance(LINK_SIZE) // Minimum link length
                .linkStrength(function (d) {
                    return d.link_power * 3;
                })
                .size([width, height]);

            // Check if the SVG exists, if it doesn't create it
            svg = d3.select(me).selectAll("svg");
            if (svg.empty()) {
                svg = d3.select(me).append("svg").attr("width", width).attr("height", height);
            }

            // Add the data, start the sim.
            force.nodes(d.nodes)
                .links(d.links)
                .start();

            // Apply stuff to links
            link = svg.selectAll(".link")
                .data(d.links);

            link.exit().remove();

            link.enter().append("line")
                .attr("class", "link")
                .style("stroke-width", 0);

            node = svg.selectAll(".node")
                .data(d.nodes)
                .attr("radius", function(d){
                    if(d.fixed){
                        return FIXED_NODE_SIZE;
                    }else {
                        return d.size * 3;
                    }
                });

            // Adding the nodes
            node.enter()
                .append("path")
                .attr("class", "node");

            node.attr("transform", function(d){ return getTranslate(d); })
                .attr("d", d3.svg.symbol()
                    .type(function(d){
                        if(d.fixed){
                            return "circle";
                        }
                        if(d.termType == "Compound"){
                            return "triangle-up";
                        }
                        else if(d.termType == "Sentence"){
                            return "square";
                        }
                        else if(d.termType == "Synonym"){
                            return "circle";
                        }
                        return "cross"; // Return a cross by default
                    })
                    .size(function(d){

                        return d.size * 2500;
                    })
                )
                .style("fill", function (d) {
                    return d3.hsl(d.color);
                })
                .on("click", function (d) {
                    // Don't run this on the fixed nodes
                    if (!d.fixed) {
                        // A clever trick to save the node and the original color
                        var originalColor = d3.select(this).style("fill");
                        var node = d3.select(this);
                        if(clickedNode != d.name){
                            // change the color of the node to purple
                            node.style("fill", d3.rgb(255, 0, 255));

                            var colorCallback = function(){
                                node.style("fill", originalColor);
                                clickedNode = null;
                            };

                            clickedNode = d.name;

                            // Have a callback function to change the color back
                            displayTerm(d, colorCallback);
                        }
                    }
                });

            text = svg.selectAll("text")
                .data(d.nodes.filter(function (d) {
                    return d.fixed;
                }));

            text.enter()
                .append("text")
                .attr("font-family", "sans-serif")
                .attr("font-size", FONT_SIZE)
                .attr("fill", "black")
                .attr("text-anchor", "middle")
                .attr("font-weight", "bold");

            text.text(function (d) {
                return d.name
            });


            // If a node is removed, remove it from the sim.
            node.exit().remove();
            text.exit().remove();

            svg.selectAll(".node").data(d.nodes).exit().remove();

            node.append("title").text(function (d) {
                return d.name;
            });

            force.on("tick", onTick);
            d3.select(window).on('resize', onResize);

            /**
             * Function that is called every tick (critical code should run fast)
             */
            var radius = 20;
            function onTick() {
                node.attr("cx", function (d) {
                        if (d.fixed) {
                            d.x = width * d.xLoc;
                            return d.x;
                        } else {
                            //d.x = Math.max(radius, Math.min(width - radius, d.x));
                            return d.x;
                        }
                    })
                    .attr("cy", function (d) {
                        if (d.fixed) {
                            d.y = height * d.yLoc;
                            return d.y;
                        } else {
                            //d.y = Math.max(radius, Math.min(height - radius, d.y));
                            return d.y;
                        }
                    })
                    .attr("x", function(d){return d.x;})
                    .attr("y", function(d){return d.y})
                    .each(collide(.5));

                console.log("Width: " + width + "Height: " + height);

                svg.selectAll(".node").attr("transform", function(d) { return getTranslate(d); });

                //console.log(graph.nodes);

                text.attr("x", function (d) {
                    return d.x;
                }).attr("y", function (d) {
                    return d.y;
                });

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
            }
        });
    }

    var maxRadius = 30,
        clusterPadding = 5;

    // Resolves collisions between d and all other circles.
    function collide(alpha){
        //console.log(graph.nodes);
        var quadtree = d3.geom.quadtree(graph.nodes);
        return function(d) {
            var r = d.radius + maxRadius + clusterPadding,
                nx1 = d.x - r,
                nx2 = d.x + r,
                ny1 = d.y - r,
                ny2 = d.y + r;
            quadtree.visit(function(quad, x1, y1, x2, y2) {
                if (quad.point && (quad.point !== d)) {
                    var x = d.x - quad.point.x,
                        y = d.y - quad.point.y,
                        l = Math.sqrt(x * x + y * y),
                        r = d.radius + quad.point.radius + clusterPadding;
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

    // Functions for setting the chart height and width.
    chart.width = function (value) {
        if (!arguments.length) return width;
        width = value;
        reapplySize();
        return chart;
    };

    chart.height = function (value) {
        if (!arguments.length) return height;
        height = value;
        reapplySize();
        return chart;
    };

    /**
     * Function called on changing the size.
     */
    function onResize() {
        chart.height(me.clientHeight - 3);
        chart.width(me.clientWidth - 3);
    }

    /**
     * Reapplies the size for all elements
     */
    function reapplySize() {
        if (force) {
            force.size([width, height]).resume();
            //force.on("tick", onTick); // With new arch I don't have to reapply the event listener
        }
        if (svg) svg.attr('width', width).attr('height', height);
    }

    function getTranslate(d){
        return"translate(" + d.x + "," + d.y + ")";
    }

    return chart;
}