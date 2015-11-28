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
 * well with more dynamic data.
 */

function forceChart() {
    // "Constant" Variables
    var FIXED_NODE_SIZE = 50;

    // "Class" Variables
    var width = 400,
        height = 400,
        force = 0,
        svg = 0,
        color = d3.scale.category20(),
        link = 0,
        node = 0,
        text = 0,
        quadTree = 0,
        graph = 0,
        me = 0;

    // This generates the chart
    function chart(selection) {
        // Generate the chart
        // "this" is the window
        // 'd' is the data
        selection.each(function (d, i) {
            graph = d;

            me = this;
            height = this.clientHeight;
            width = this.clientWidth;

            force = d3.layout.force()
                .charge(-150)
                .linkStrength(function(d){return d.link_power;})
                .size([width, height]);

            // Check if the SVG exists, if it doesn't create it
            svg = d3.select(this).selectAll("svg");
            if(svg.empty()){
                d3.select(this).append("svg").attr("width", width).attr("height", height);
            }

            quadTree = d3.geom.quadtree(d.nodes);

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
                .style("stroke-width", 3);

            node = svg.selectAll(".node")
                .data(d.nodes);

            // Adding the nodes
            node.enter().append("circle")
                .attr("class", "node")
                .attr("r", function(d){
                    if(d.fixed){
                        return FIXED_NODE_SIZE;
                    }else{
                        return d.size;
                    }
                })
                .attr("radius", function(d){return d.r;})
                .style("fill", function (d) {
                    return d3.rgb(d.color);
                })
                .on("click", function(d){
                    displayDocument(d.docId);
                });

            text = svg.selectAll("text").data(d.nodes.filter(function(d){return d.fixed;}));
            text.enter()
                .append("text").text(function(d){
                    return d.name;
                })
                .attr("font-family", "sans-serif")
                .attr("font-size", "12px")
                .attr("fill", "black")
                .attr("text-anchor", "middle");


            // If a node is removed, remove it from the sim.
            node.exit().remove();

            svg.selectAll(".node").data(d.nodes).exit().remove();

            node.append("title").text(function (d) {
                return d.name;
            });

            force.on("tick", onTick);
            d3.select(window).on('resize', onResize);
        });
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
        chart.height(me.clientHeight);
        chart.width(me.clientWidth);
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

    /**
     * Function that is called every tick (critical code should run fast)
     */
    function onTick() {
        var r = 5;

        node.each(collide(.5))
            .attr("cx", function (d) {
            if(d.fixed){
                return d.x = width * d.xLoc;
            }else{
                return d.x;
            }
        })
            .attr("cy", function (d) {
                if(d.fixed){
                    return d.y = height * d.yLoc;
                }else{
                    return d.y;
                }
        });

        text.attr("x", function(d){
            return d.x;
        }).attr("y", function(d){
            return d.y;
        })

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


    var clusterPadding = 20, // separation between different-color nodes
        maxRadius = 50,
        padding = 1.5;

    /**
     * This function is used to collide every node. // TODO: I have no idea how this works and I need to figure it out.
     * @param alpha
     * @returns {Function}
     */
    function collide(alpha) {
        var quadtree = d3.geom.quadtree(graph.nodes);
        return function (d) {
            var r = d.size + maxRadius + Math.max(padding, clusterPadding),
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

    return chart;
}