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
    var LINK_SIZE = 30,
        FONT_SIZE = "16px";
    // "Class" Variables
    var width = 400,
        height = 400,
        force = 0,
        svg = 0,
        defs = 0,
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

            // Find blank links, which give the error
            // "Uncaught TypeError: Cannot read property 'weight' of undefined"
            graph.links.forEach(function(link, index, list) {
                if (typeof graph.nodes[link.source] === 'undefined') {
                    console.log('undefined source', link);
                }
                if (typeof graph.nodes[link.target] === 'undefined') {
                    console.log('undefined target', link);
                }
            });

            //for(var j = 0; j < graph.nodes.length; j++){
            //    if(d.nodes[j].fixed){
            //        d.nodes[j].radius = d.nodes[j].size * 15;
            //    }else{
            //        d.nodes[j].radius = d.nodes[j].size * 35;
            //    }
            //
            //}

            force = d3.layout.force()
                .nodes(d.nodes)
                .links(d.links)
                .charge(-50)
                .size([width, height]);

            // Check if the SVG exists, if it doesn't create it
            svg = d3.select(me).selectAll("svg");
            if (svg.empty()) {
                svg = d3.select(me).append("svg").attr("width", width).attr("height", height);
            }

            var defs = svg.append("defs");

            // Add the data, start the sim.
            force.linkDistance(LINK_SIZE) // Minimum link length
                .linkStrength(function (d) {
                    return d.link_power * 5;
                })
                .start();

            // Apply stuff to links
            link = svg.selectAll(".link")
                .data(d.links);

            link.exit().remove();

            link.enter().append("line")
                .attr("class", "link")
                .style("stroke-width", function(d){
                    if(d.source.id == -999){
                        return 0;
                    }
                    return "0";
                })
                .style("stroke", function(d){ return d.source.color});

            node = svg.selectAll(".node")
                .data(d.nodes);

            // Adding the nodes
            node.enter()
                .append("path")
                .attr("class", "node")
                .attr("transform", function(d){ return getTranslate(d); })
                .attr("d", d3.svg.symbol()
                    .type(function(d){
                        if(d.fixed){
                            return "circle";
                        }
                        if(d.termType == "Compound"){
                            return "square";
                        }
                        else if(d.termType == "Sentence"){
                            return "triangle-up";
                        }
                        else if(d.termType == "Synonym"){
                            return "circle";
                        }
                        else if(d.termType == "Semantic"){
                            return "triangle-down";
                        }
                        return "cross"; // Return a cross by default
                    })
                    .size(function(d){
                        return d.size * 2500;
                    }))
                .attr("fill", function (d) { // TODO: Implement a pattern fill technique here.
                    if(d.id >= 0){
                        var totalHeight = Math.ceil(this.getBBox().height);

                        var perUnitHeight = totalHeight / d.colors.length;
                        var height = 0;
                        var pattern = defs.append("pattern")
                            .attr("id", "id"+ d.id)
                            .attr("width", 10)
                            .attr("x", 0)
                            .attr("y", d.colors.length % 2 == 0 ? 0 : (1.0/2.0)*perUnitHeight )
                            .attr("patternUnits", "userSpaceOnUse");

                        for(var color in d.colors){
                            var currentColor = d.colors[color];
                            pattern.append("rect")
                                .attr("height", perUnitHeight)
                                .attr("width", 10)
                                .attr("x", 0)
                                .attr("y", height)
                                .attr("fill", currentColor);
                            height += perUnitHeight;
                        }

                        pattern.attr("height", height);

                        return "url(#id"+ d.id + ")";
                    } else{
                        return d3.hsl(d.color);
                    }
                })
                .attr("id", function(d){
                    return "id" + d.id;
                })
                .attr("radius", function(d) {
                    d.radius = Math.ceil(this.getBBox().height / 2);
                    return d.radius;
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

                svg.selectAll(".node").attr("transform", function(d) { return getTranslate(d); });

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

    var maxRadius = 0,
        clusterPadding = 5;

    // Resolves collisions between d and all other circles.
    function collide(alpha){
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