/*
 * Copyright (C) 2014 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.graph;

import com.google.common.annotations.Beta;

import java.util.Set;

import javax.annotation.Nullable;

/**
 * A network consisting of a set of nodes of type N and a set of edges of type E.
 * That is, a subtype of {@link Graph} that represents edges as explicit first-class objects.
 * Users that are not interested in edges as first-class objects should use a {@link Graph}
 * instead.
 *
 * <p>Users that wish to modify a {@code Network} must work with its subinterface,
 * {@code MutableNetwork}.
 *
 * <p>This interface permits, but does not enforce, any of the following variations of graphs:
 * <ul>
 * <li>directed and undirected edges
 * <li>nodes and edges with attributes (for example, weighted edges)
 * <li>nodes and edges of different types (for example, bipartite or multimodal graphs)
 * <li>parallel edges (multiple edges which connect a single set of nodes)
 * </ul>
 *
 * <p>Extensions or implementations of this interface may enforce or disallow any or all
 * of these variations.
 *
 * <p>Definitions:
 * <ul>
 * <li>{@code edge} and {@code node} are <b>incident</b> to each other if the set of
 *     {@code edge}'s endpoints includes {@code node}.
 * <li>{@code nodeA} and {@code nodeB} are mutually <b>adjacent</b> if both are incident
 *     to a common {@code edge}.
 *     <br>Similarly, {@code edge1} and {@code edge2} are mutually adjacent if both are
 *     incident to a common {@code node}.
 * <li>Elements are <b>connected</b> if they are either incident or adjacent.
 * <li>{@code edge} is an <b>incoming edge</b> of a {@code node} if it can be traversed (in
 *     the direction, if any, of {@code edge}) from a node adjacent to {@code node}.
 * <li>{@code edge} is an <b>outgoing edge</b> of {@code node} if it can be traversed (in
 *     the direction, if any, of {@code edge}) from {@code node} to reach a node adjacent to
 *     {@code node}.
 *   <ul>
 *   <li>Note: <b>undirected</b> edges are both incoming and outgoing edges of a {@code node},
 *       while <b>directed</b> edges are either incoming or outgoing edges of {@code node}
 *       (and not both, unless the edge is a self-loop).
 *       <br>Thus, in the following example {@code edge1} is an incoming edge of {@code nodeB} and
 *       an outgoing edge of {@code nodeA}, while {@code edge2} is both an incoming and an outgoing
 *       edge of both {@code node3} and {@code node4}:
 *       <br><pre><code>
 *         directedGraph.addEdge(edge1, nodeA, nodeB);
 *         undirectedGraph.addEdge(edge2, node3, node4);
 *       </pre></code>
 *   </ul>
 * <li>A node {@code pred} is a <b>predecessor</b> of {@code node} if it is incident to an incoming
 *     {@code edge} of {@code node} (and is not itself {@code node} unless {@code edge} is
 *     a self-loop).
 * <li>A node {@code succ} is a <b>successor</b> of {@code node} if it is incident to an outgoing
 *     {@code edge} of {@code node} (and is not itself {@code node} unless {@code edge} is
 *     a self-loop).
 * <li>Directed edges only:
 *   <ul>
 *   <li>{@code node} is a <b>source</b> of {@code edge} if {@code edge} is an outgoing edge
 *       of {@code node}.
 *   <li>{@code node} is a <b>target</b> of {@code edge} if {@code edge} is an incoming edge
 *       of {@code node}.
 *   </ul>
 * </ul>
 *
 * <p>General notes:
 * <ul>
 * <li><b>Nodes/edges must be useable as {@code Map} keys</b>:
 *   <ul>
 *   <li>They must be unique in a graph: nodes {@code nodeA} and {@code nodeB} are considered
 *       different if and only if {@code nodeA.equals(nodeB) == false}, and the same for edges.
 *   <li>If you would otherwise have duplicate edges (e.g. weighted edges represented by a Double),
 *       you can instead wrap the edges in a custom class that defers to {@link Object} for its
 *       {@code equals()} and {@code hashCode()} implementations.
 *   <li>If graph elements have mutable state:
 *     <ul>
 *     <li>the mutable state must not be reflected in the {@code equals/hashCode} methods
 *         (this is discussed in the {@code Map} documentation in detail)
 *     <li>don't construct multiple elements that are equal to each other and expect them to be
 *         interchangeable.  In particular, when adding such elements to a graph, you should create
 *         them once and store the reference if you will need to refer to those elements more than
 *         once during creation (rather than passing {@code new MyMutableNode(id)} to each
 *         {@code add*()} call).
 *     </ul>
 *   </ul>
 *   <br>Generally speaking, your design may be more robust if you use immutable nodes/edges and
 * store mutable per-element state in a separate data structure (e.g. an element-to-state map).
 * <li>There are no Node or Edge classes built in.  So you can have a {@code Graph<Integer, String>}
 *     or a {@code Graph<Author,Publication>} or a {@code Graph<Webpage,Link>}.
 * </ul>
 *
 * <p>Notes on accessors:
 * <ul>
 * <li>Accessors which return collections may return views of the Graph. Modifications to the graph
 *     which affect a view (e.g. calling {@code addNode(n)} or {@code removeNode(n)} while iterating
 *     through {@code nodes()}) are not supported and may result in ConcurrentModificationException.
 * <li>Accessors which return collections will return empty collections if their inputs are valid
 *     but no elements satisfy the request (for example: {@code adjacentNodes(node)} will return an
 *     empty collection if {@code node} has no adjacent nodes).
 * <li>Accessors will throw {@code IllegalArgumentException} if passed a node/edge
 *     that is not in the graph.
 * <li>Accessors take Object parameters rather than N/E generic type specifiers to match the pattern
 *     set by the Java Collections Framework.
 * </ul>
 *
 * <p>Notes for implementors:
 * <ul>
 * <li>Implementations have numerous options for internal representations: matrices, adjacency
 *     lists, adjacency maps, etc.
 * <li>For accessors that return a {@code Set}, there are several options for the set behavior,
 *     including:
 *     <ol>
 *     <li>Set is an immutable copy (e.g. {@code ImmutableSet}): attempts to modify the set in any
 *         way will throw an exception, and modifications to the graph will <b>not</b> be reflected
 *         in the set.
 *     <li>Set is an unmodifiable view (e.g. {@code Collections.unmodifiableSet()}): attempts to
 *         modify the set in any way will throw an exception, and modifications to the graph will be
 *         reflected in the set.
 *     <li>Set is a mutable copy: it may be modified, but modifications to the graph will <b>not</b>
 *         be reflected in the set, and vice versa.
 *     <li>Set is a modifiable view: it may be modified, and modifications to the graph will be
 *         reflected in the set (but modifications to the set will <b>not</b> be reflected in the
 *         graph).
 *     <li>Set exposes the internal data directly: it may be modified, and modifications to the
 *         graph will be reflected in the set, and vice versa.
 *     </ol>
 *     Note that (1) and (2) are generally preferred. (5) is generally a hazardous design choice
 *     and should be avoided, because keeping the internal data structures consistent can be tricky.
 * <li>Prefer extending {@link AbstractGraph} over implementing {@link Graph} directly. This will
 *     ensure that the implementations of {@link #equals(Object)} and
 *     {@link #hashCode()} are mutually consistent, and consistent across
 *     implementations.
 * <li>{@code Multimap}s are not sufficient internal data structures for Graph implementations
 *     that support isolated nodes (nodes that have no incident edges), due to their restriction
 *     that a key either maps to at least one value, or is not present in the {@code Multimap}.
 * </ul>
 *
 * <p>Examples of use:
 * <ul>
 * <li>Is {@code node} in the graph?
 * <pre><code>
 *   graph.nodes().contains(node)
 * </code></pre>
 * <li>Traversing an undirected graph node-wise:
 * <pre><code>
 *   // Visit nodes reachable from {@code node}.
 *   void depthFirstTraverse(N node) {
 *     if (!isVisited(node)) {
 *       visit(node);
 *       for (N successor : graph.successors(node)) {
 *         depthFirstTraverse(successor);
 *       }
 *     }
 *   }
 * </code></pre>
 * <li>Traversing a directed graph edge-wise:
 * <pre><code>
 *   // Update the shortest-path distances of the successors to {@code node}
 *   // in a directed graph (inner loop of Dijkstra's algorithm):
 *   void updateDistances(N node) {
 *     nodeDistance = distances.get(node);
 *     for (E outEdge : graph.outEdges(node)) {
 *       N target = graph.target(outEdge);
 *       double targetDistance = nodeDistance + outEdge.getWeight();
 *       if (targetDistance < distances.get(target)) {
 *         distances.put(target, targetDistance);
 *       }
 *     }
 *   }
 * </code></pre>
 * </ul>
 *
 * @author James Sexton
 * @author Joshua O'Madadhain
 * @param <N> Node parameter type
 * @param <E> Edge parameter type
 * @since 20.0
 */
@Beta
public interface Network<N, E> extends Graph<N> {
  /**
   * Returns all edges in this network, in the order specified by {@link #edgeOrder()}.
   */
  Set<E> edges();

  //
  // Graph properties
  //

  /**
   * {@inheritDoc}
   *
   * <p>A directed edge is an {@linkplain #outEdges(Object) outgoing edge} of its
   * {@linkplain Endpoints#source() source}, and an {@linkplain #inEdges(Object) incoming edge}
   * of its {@linkplain Endpoints#target() target}. An undirected edge connects its
   * {@linkplain #incidentNodes(Object) incident nodes} to each other, and is both an
   * {@linkplain #outEdges(Object) outgoing edge} and {@linkplain #inEdges(Object) incoming edge}
   * of each incident node.
   */
  @Override
  boolean isDirected();

  /**
   * Returns true if this graph allows parallel edges. Attempting to add a parallel edge to a graph
   * that does not allow them will throw an {@link UnsupportedOperationException}.
   */
  boolean allowsParallelEdges();

  /**
   * Returns the order of iteration for the elements of {@link #edges()}.
   */
  ElementOrder<? super E> edgeOrder();

  //
  // Element-level accessors
  //

  /**
   * Returns the edges whose endpoints in this graph include {@code node}.
   *
   * @throws IllegalArgumentException if {@code node} is not an element of this graph
   */
  Set<E> incidentEdges(Object node);

  /**
   * Returns the nodes which are the endpoints of {@code edge} in this graph as {@link Endpoints}.
   *
   * @throws IllegalArgumentException if {@code edge} is not an element of this graph
   */
  Endpoints<N> incidentNodes(Object edge);

  /**
   * Returns the edges which have an {@linkplain #incidentNodes(Object) incident node}
   * in common with {@code edge} in this graph.
   *
   * <p>Whether an edge is considered adjacent to itself is not defined by this interface, but
   * generally edges are not considered to be self-adjacent.
   *
   * @throws IllegalArgumentException if {@code edge} is not an element of this graph
   */
  Set<E> adjacentEdges(Object edge);

  /**
   * Returns the set of edges that connect {@code nodeA} to {@code nodeB}.
   *
   * <p>This set is the intersection of {@code outEdges(nodeA)} and {@code inEdges(nodeB)}. If
   * {@code nodeA} is equal to {@code nodeB}, then it is the set of self-loop edges for that node.
   *
   * @throws IllegalArgumentException if {@code nodeA} or {@code nodeB} is not an element
   *     of this graph
   */
  Set<E> edgesConnecting(Object nodeA, Object nodeB);

  /**
   * Returns all edges in this graph which can be traversed in the direction (if any) of the edge
   * to end at {@code node}.
   *
   * @throws IllegalArgumentException if {@code node} is not an element of this graph
   */
  Set<E> inEdges(Object node);

  /**
   * Returns all edges in this graph which can be traversed in the direction (if any) of the edge
   * starting from {@code node}.
   *
   * @throws IllegalArgumentException if {@code node} is not an element of this graph
   */
  Set<E> outEdges(Object node);

  //
  // Element-level queries
  //

  /**
   * {@inheritDoc}
   *
   * <p>Equivalent to {@code incidentEdges(node).size()}.
   */
  @Override
  int degree(Object node);

  /**
   * {@inheritDoc}
   *
   * <p>Equivalent to {@code inEdges(node).size()}.
   */
  @Override
  int inDegree(Object node);

  /**
   * {@inheritDoc}
   *
   * <p>Equivalent to {@code outEdges(node).size()}.
   */
  @Override
  int outDegree(Object node);

  /**
   * Returns {@code true} iff {@code object} is a graph that has the same node/edge relationships
   * as those in this graph.
   *
   * <p>Thus, two graphs A and B are equal if <b>all</b> of the following are true:
   * <ul>
   * <li>A and B have the same node set
   * <li>A and B have the same edge set
   * <li>A and B have the same incidence relationships, e.g., for each node/edge in A and in B
   *     its incident edge/node set in A is the same as its incident edge/node set in B.
   *     <br>Thus, every edge in A and B connect the same nodes in the same direction (if any).
   * </ul>
   *
   * <p>Graph properties are <b>not</b> respected by this method. For example, two graphs may be
   * considered equal even if one allows parallel edges and the other doesn't. Additionally, the
   * order in which edges or nodes are added to the graph, and the order in which they are iterated
   * over, are irrelevant.
   *
   * <p>A reference implementation of this is provided by
   * {@link Graphs#equal(Network, Network)}.
   */
  @Override
  boolean equals(@Nullable Object object);

  /**
   * Returns the hash code for this graph. The hash code of a graph is defined as the hash code
   * of a map from each of the graph's nodes to their incident edges.
   *
   * <p>A reference implementation of this is provided by {@link Graphs#hashCode(Graph)}.
   *
   * <p>Note that by this definition, two graphs that are equal in every aspect except edge
   * direction will have the same hash code (but can still be differentiated by
   * {@link #equals(Object)}).
   */
  @Override
  int hashCode();
}
