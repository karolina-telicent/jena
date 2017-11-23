/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.arq.querybuilder.clauses;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.WhereValidator;
import org.apache.jena.graph.FrontsTriple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.LiteralLabelFactory;
import org.apache.jena.query.Query;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.expr.E_Random;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementMinus;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementUnion;
import org.apache.jena.vocabulary.RDF;
import org.junit.After;

import static org.junit.Assert.*;

import org.xenei.junit.contract.Contract;
import org.xenei.junit.contract.ContractTest;
import org.xenei.junit.contract.IProducer;

@Contract(WhereClause.class)
public class WhereClauseTest<T extends WhereClause<?>> extends
		AbstractClauseTest {

	// the producer we will user
	private IProducer<T> producer;

	@Contract.Inject
	// define the method to set producer.
	public final void setProducer(IProducer<T> producer) {
		this.producer = producer;
	}

	protected final IProducer<T> getProducer() {
		return producer;
	}

	@After
	public final void cleanupWhereClauseTest() {
		getProducer().cleanUp(); // clean up the producer for the next run
	}

	@ContractTest
	public void testAddWhereStrings() {
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addWhere("<one>",
				"<two>", "three");
		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + SPACE
				+ uri("two") + SPACE + quote("three") + presentStringType()
				+ OPT_SPACE + CLOSE_CURLY, builder.buildString());
	}

	@ContractTest
	public void testAddOptionalString() {
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addOptional("<one>",
				"<two>", "three");
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE
				+ quote("three") + presentStringType() + OPT_SPACE
				+ CLOSE_CURLY + CLOSE_CURLY, builder.buildString());

	}

	@ContractTest
	public void testAddOptionalStringWithPath() {
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addOptional("<one>",
				"<two>/<dos>", "three");
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + uri("one") + SPACE + uri("two")+"/"+uri("dos") + SPACE
				+ quote("three") + presentStringType() + OPT_SPACE
				+ CLOSE_CURLY + CLOSE_CURLY, builder.buildString());

	}
	
	@ContractTest
	public void testAddOptionalObjects() {
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addOptional(
				NodeFactory.createURI("one"), NodeFactory.createURI("two"),
				NodeFactory.createURI("three"));
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE
				+ uri("three") + OPT_SPACE + CLOSE_CURLY,
				builder.buildString());
	}

	@ContractTest
	public void testAddOptionalTriple() {
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addOptional(
				NodeFactory.createURI("one"), NodeFactory.createURI("two"),
				NodeFactory.createURI("three"));

		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE
				+ uri("three") + OPT_SPACE + CLOSE_CURLY,
				builder.buildString());
	}
	
	@ContractTest
	public void testAddOptionalTriplePath() {
		WhereClause<?> whereClause = getProducer().newInstance();
		PrefixMapping pmap = new PrefixMappingImpl();
		pmap.setNsPrefix("ts", "urn:test:");
		Path path = PathParser.parse( "ts:two/ts:dos", pmap);
		AbstractQueryBuilder<?> builder = whereClause.addOptional(new TriplePath(
				NodeFactory.createURI("one"), path,
				NodeFactory.createURI("three")));

		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + uri("one") + SPACE + uri("urn:test:two")+"/"+uri("urn:test:dos") + SPACE
				+ uri("three") + OPT_SPACE + CLOSE_CURLY,
				builder.buildString());
	}
	
	@ContractTest
	public void testAddOptionalObjectsWithPath() {
		WhereClause<?> whereClause = getProducer().newInstance();
		PrefixMapping pmap = new PrefixMappingImpl();
		pmap.setNsPrefix("ts", "urn:test:");
		Path path = PathParser.parse( "ts:two/ts:dos", pmap);
		AbstractQueryBuilder<?> builder = whereClause.addOptional(
				NodeFactory.createURI("one"), path,
				NodeFactory.createURI("three"));

		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + uri("one") + SPACE + uri("urn:test:two")+"/"+uri("urn:test:dos") + SPACE
				+ uri("three") + OPT_SPACE + CLOSE_CURLY,
				builder.buildString());
	}

	@ContractTest
	public void testAddOptionalGroupPattern() throws ParseException {
		
		Var s = Var.alloc("s" );
		Node q = NodeFactory.createURI( "urn:q" );
		Node v = NodeFactory.createURI( "urn:v" );
		Var x = Var.alloc("x");
		Node n123 = NodeFactory.createLiteral(LiteralLabelFactory.createTypedLiteral(123));	
		
		SelectBuilder pattern = new SelectBuilder();
		pattern.addWhere( new Triple( s, q,  n123 ) );
		pattern.addWhere( new Triple( s, v, x));

		
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addOptional( pattern );

		ElementPathBlock epb = new ElementPathBlock();
		ElementOptional optional = new ElementOptional(epb);
		TriplePath tp = new TriplePath( new Triple(s, q, n123));
		epb.addTriplePath( tp );
		 tp = new TriplePath( new Triple(s, v, x));
		epb.addTriplePath( tp );
		
		WhereValidator visitor = new WhereValidator( optional );
		builder.build().getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );
		
	}

	@ContractTest
	public void testAddFilter() throws ParseException {
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addFilter("?one<10");

		assertContainsRegex(WHERE + OPEN_CURLY + "FILTER" + OPT_SPACE
				+ OPEN_PAREN + var("one") + OPT_SPACE + LT + OPT_SPACE + "10"
				+ CLOSE_PAREN + CLOSE_CURLY, builder.buildString());
	}

	@ContractTest
	public void testAddSubQuery() {
		SelectBuilder sb = new SelectBuilder();
		sb.addPrefix("pfx", "urn:uri:").addVar("?x")
				.addWhere("pfx:one", "pfx:two", "pfx:three");
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addSubQuery(sb);
		Query query = builder.build();

		Query q2 = new Query();
		q2.setQuerySelectType();
		q2.addProjectVars( Arrays.asList( Var.alloc( "x")));
		ElementPathBlock epb = new ElementPathBlock();
		q2.setQueryPattern(epb);
		epb.addTriplePath( new TriplePath( new Triple( NodeFactory.createURI( "urn:uri:one"),
				NodeFactory.createURI( "urn:uri:two"),NodeFactory.createURI( "urn:uri:three"))));
		ElementSubQuery esq = new ElementSubQuery( q2 );
		
		WhereValidator visitor = new WhereValidator( esq );
		query.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );

	}

	@ContractTest
	public void testAddUnion() {
		SelectBuilder sb = new SelectBuilder();
		sb.addPrefix("pfx", "uri").addVar("?x")
				.addWhere("<one>", "<two>", "three");
		WhereClause<?> whereClause = getProducer().newInstance();
		whereClause.getWhereHandler().addWhere( new TriplePath(Triple.ANY));
		AbstractQueryBuilder<?> builder = whereClause.addUnion(sb);

		String str = builder.buildString();
		assertContainsRegex(PREFIX + "pfx:" + SPACE + uri("uri") + SPACE, str );
		assertContainsRegex( WHERE + OPEN_CURLY + OPEN_CURLY+"ANY"+SPACE+"ANY"+SPACE+"ANY"+CLOSE_CURLY
				+ SPACE+UNION + OPEN_CURLY + SELECT + var("x") + SPACE + WHERE
				+ OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE
				+ quote("three") + presentStringType() + OPT_SPACE
				+ CLOSE_CURLY + CLOSE_CURLY, str);

	}

	@ContractTest
	public void testSetVarsInTriple() {
		Var v = Var.alloc("v");
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addWhere(new Triple(
				NodeFactory.createURI("one"), NodeFactory.createURI("two"), v));
		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + SPACE
				+ uri("two") + SPACE + var("v") + OPT_SPACE
				+ CLOSE_CURLY, builder.buildString());

		builder.setVar(v, NodeFactory.createURI("three"));

		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + SPACE
				+ uri("two") + SPACE + uri("three") + OPT_SPACE
				+ CLOSE_CURLY, builder.buildString());

		builder.setVar(v, NodeFactory.createURI("four"));

		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + SPACE
				+ uri("two") + SPACE + uri("four") + OPT_SPACE
				+ CLOSE_CURLY, builder.buildString());

		builder.setVar(v, null);

		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + SPACE
				+ uri("two") + SPACE + var("v") + OPT_SPACE
				+ CLOSE_CURLY, builder.buildString());

	}

	@ContractTest
	public void testSetVarsInFilter() throws ParseException {
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addFilter("?one < ?v");
		assertContainsRegex(WHERE + OPEN_CURLY + "FILTER" + OPT_SPACE
				+ OPEN_PAREN + var("one") + OPT_SPACE + LT + OPT_SPACE
				+ var("v") + CLOSE_PAREN + CLOSE_CURLY, builder.buildString());

		builder.setVar(Var.alloc("v"), NodeFactory
				.createLiteral(LiteralLabelFactory.createTypedLiteral(10)));

		assertContainsRegex(WHERE + OPEN_CURLY + "FILTER" + OPT_SPACE
				+ OPEN_PAREN + var("one") + OPT_SPACE + LT + OPT_SPACE
				+ quote("10") + "\\^\\^"
				+ uri("http://www.w3.org/2001/XMLSchema#int") + CLOSE_PAREN
				+ CLOSE_CURLY, builder.buildString());

	}

	@ContractTest
	public void testSetVarsInOptional() {
		Var v = Var.alloc("v");
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addOptional(new Triple(
				NodeFactory.createURI("one"), NodeFactory.createURI("two"), v));
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE
				+ var("v") + OPT_SPACE + CLOSE_CURLY + CLOSE_CURLY,
				builder.buildString());

		builder.setVar(v, NodeFactory.createURI("three"));
		assertContainsRegex(WHERE + OPEN_CURLY + "OPTIONAL" + SPACE
				+ OPEN_CURLY + uri("one") + SPACE + uri("two") + SPACE
				+ uri("three") + OPT_SPACE + CLOSE_CURLY + CLOSE_CURLY,
				builder.buildString());
	}

	@ContractTest
	public void testSetVarsInSubQuery() {
		Var v = Var.alloc("v");
		SelectBuilder sb = new SelectBuilder();
		sb.addPrefix("pfx", "uri").addWhere("<one>", "<two>", v);
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addSubQuery(sb);

		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + SPACE
				+ uri("two") + SPACE + var("v") + CLOSE_CURLY,
				builder.buildString());

		builder.setVar(v, NodeFactory.createURI("three"));
		assertContainsRegex(WHERE + OPEN_CURLY + uri("one") + SPACE
				+ uri("two") + SPACE + uri("three") + CLOSE_CURLY,
				builder.buildString());
	}

	@ContractTest
	public void testSetVarsInUnion() {
		Var v = Var.alloc("v");
		SelectBuilder sb1 = new SelectBuilder()
		.addPrefix("pfx", "uri").addWhere("<one>", "<two>", v);
		WhereClause<?> whereClause = getProducer().newInstance();
		whereClause.addUnion(sb1);		
		SelectBuilder sb2 = new SelectBuilder().addWhere("<uno>", "<dos>", "<tres>");
		AbstractQueryBuilder<?> builder = whereClause.addUnion(sb2);
		Query query = builder.build();
		
		Node one = NodeFactory.createURI("one");
		Node two = NodeFactory.createURI("two");
		Node three = NodeFactory.createURI("three");
		Node uno = NodeFactory.createURI("uno");
		Node dos = NodeFactory.createURI("dos");
		Node tres = NodeFactory.createURI("tres");
		
		ElementUnion union = new ElementUnion();
		ElementPathBlock epb = new ElementPathBlock();
		Triple t = new Triple( one, two, v.asNode());
		epb.addTriple(t);
		union.addElement(epb);
		ElementPathBlock epb2 = new ElementPathBlock();
		t = new Triple( uno, dos, tres);
		epb2.addTriple(t);
		union.addElement(epb2);
		WhereValidator visitor = new WhereValidator( union );
		query.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );

		builder.setVar(v, NodeFactory.createURI("three"));
		query = builder.build();
		
		union = new ElementUnion();
		 epb = new ElementPathBlock();
		 t = new Triple( one, two, three);
		epb.addTriple(t);
		union.addElement(epb);
		 epb2 = new ElementPathBlock();
		t = new Triple( uno, dos, tres);
		epb2.addTriple(t);
		union.addElement(epb2);
		 visitor = new WhereValidator( union );
			query.getQueryPattern().visit( visitor );
			assertTrue( visitor.matching );

	}

	@ContractTest
	public void testBindStringVar() throws ParseException {
		Var v = Var.alloc("foo");
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addBind("rand()", v);
		Query query = builder.build();
		
		ElementBind bind = new ElementBind( v, new E_Random());
		WhereValidator visitor = new WhereValidator( bind );
		query.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );
		
		Node three = NodeFactory.createURI("three");
		builder.setVar(v, three );
		query = builder.build();
		
		visitor = new WhereValidator( new ElementTriplesBlock() );
		query.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );

	}

	@ContractTest
	public void testBindExprVar() {
		Var v = Var.alloc("foo");
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause
				.addBind(new E_Random(), v);
		Query query = builder.build();
		

		WhereValidator visitor = new WhereValidator( new ElementBind( Var.alloc("foo"), new E_Random() ) );
		query.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );
		
		
		builder.setVar(v, NodeFactory.createURI("three"));
		query = builder.build();
		
		visitor = new WhereValidator( new ElementTriplesBlock() );
		query.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );
		
	}
	
	@ContractTest
	public void testList() {
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addWhere(whereClause.list( "<one>", "?two", "'three'"),
		                                                       "<foo>", "<bar>");
	
		Query query = builder.build();

		Node one = NodeFactory.createURI("one");
		Node two = Var.alloc("two").asNode();
		Node three = NodeFactory.createLiteral( "three");
		Node foo = NodeFactory.createURI("foo");
		Node bar = NodeFactory.createURI("bar");
		
		ElementPathBlock epb = new ElementPathBlock();
		Node firstObject = NodeFactory.createBlankNode();		
		Node secondObject = NodeFactory.createBlankNode();
		Node thirdObject = NodeFactory.createBlankNode();
		
		epb.addTriplePath( new TriplePath( new Triple( firstObject, RDF.first.asNode(), one)));
		epb.addTriplePath( new TriplePath( new Triple( firstObject, RDF.rest.asNode(), secondObject)));
		epb.addTriplePath( new TriplePath( new Triple( secondObject, RDF.first.asNode(), two)));
		epb.addTriplePath( new TriplePath( new Triple( secondObject, RDF.rest.asNode(), thirdObject)));
		epb.addTriplePath( new TriplePath( new Triple( thirdObject, RDF.first.asNode(), three)));
		epb.addTriplePath( new TriplePath( new Triple( thirdObject, RDF.rest.asNode(), RDF.nil.asNode())));
		epb.addTriplePath( new TriplePath( new Triple( firstObject, foo, bar)));
		
		
		WhereValidator visitor = new WhereValidator( epb );
		query.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );
		
	}
	
	@ContractTest
	public void testAddMinus() {
		SelectBuilder sb = new SelectBuilder();
		sb.addPrefix("pfx", "uri").addVar("?x")
				.addWhere("<one>", "<two>", "three");
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addMinus( sb );
		
		Query query = builder.build();
		
		ElementPathBlock epb = new ElementPathBlock();
		ElementMinus minus = new ElementMinus(epb);
		epb.addTriplePath( new TriplePath( new Triple( NodeFactory.createURI("one"), NodeFactory.createURI("two"), NodeFactory.createLiteral("three"))));
		WhereValidator visitor = new WhereValidator( minus );
		query.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );
	}

	@ContractTest
	public void testAddGraph_frontsTriple()
	{
		final Node s = NodeFactory.createURI( "s");
		final Node p = NodeFactory.createURI( "p");
		final Node o = NodeFactory.createURI( "o");

		FrontsTriple ft = new FrontsTriple(){

			@Override
			public Triple asTriple() {
				return new Triple( s, p, o );
			}};
		
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addGraph( "<g>", ft );
		Query query = builder.build();

		ElementPathBlock epb = new ElementPathBlock();
		ElementNamedGraph eng = new ElementNamedGraph( NodeFactory.createURI("g"), epb );
		epb.addTriplePath( new TriplePath( new Triple( s, p, o)));
		
		WhereValidator visitor = new WhereValidator( eng );
		query.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );
	}
	

	public void addGraph_GSPO() {
		final Node s = NodeFactory.createURI( "s");
		final Node p = NodeFactory.createURI( "p");
		final Node o = NodeFactory.createURI( "o");

		
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addGraph( "<g>", s, p, o );
		Query query = builder.build();

		ElementPathBlock epb = new ElementPathBlock();
		ElementNamedGraph eng = new ElementNamedGraph( NodeFactory.createURI("g"), epb );
		epb.addTriplePath( new TriplePath( new Triple( s, p, o)));
		
		WhereValidator visitor = new WhereValidator( eng );
		query.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );
	}

	@ContractTest
	public void testAddGraph_triple()
	{
		final Node s = NodeFactory.createURI( "s");
		final Node p = NodeFactory.createURI( "p");
		final Node o = NodeFactory.createURI( "o");

		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addGraph( "<g>", new Triple( s, p, o ) );
		Query query = builder.build();

		ElementPathBlock epb = new ElementPathBlock();
		ElementNamedGraph eng = new ElementNamedGraph( NodeFactory.createURI("g"), epb );
		epb.addTriplePath( new TriplePath( new Triple( s, p, o)));
		
		WhereValidator visitor = new WhereValidator( eng );
		query.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );
	}	
	
	@ContractTest
	public void testAddGraph_triplePath()
	{
		final Node s = NodeFactory.createURI( "s");
		final Node p = NodeFactory.createURI( "p");
		final Node o = NodeFactory.createURI( "o");
		TriplePath tp = new TriplePath( new Triple( s, p, o));
		
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addGraph( "<g>", tp );
		Query query = builder.build();

		ElementPathBlock epb = new ElementPathBlock();
		ElementNamedGraph eng = new ElementNamedGraph( NodeFactory.createURI("g"), epb );
		epb.addTriplePath( new TriplePath( new Triple( s, p, o)));
		
		WhereValidator visitor = new WhereValidator( eng );
		query.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );
	}	

	@ContractTest
	public void testAddWhereValueVar_var()
	{
		final Var v = Var.alloc("v");
				
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder =  whereClause.addWhereValueVar( "?v" );
				
		Query query = builder.build();

		ElementData edat = new ElementData();
		edat.add( v );
		
		WhereValidator visitor = new WhereValidator( edat );
		query.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );
	}		

	@ContractTest
	public void testAddWhereValueVar_var_values()
	{
		final Var v = Var.alloc("v");
				
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder =  whereClause.addWhereValueVar( "?v", "<one>" );
				
		Query query = builder.build();

		BindingHashMap binding = new BindingHashMap();
		binding.add( v, NodeFactory.createURI( "one" ));
		ElementData edat = new ElementData();
		edat.add( v );
		edat.add( binding );
		WhereValidator visitor = new WhereValidator( edat );
		query.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );
	}	

	@ContractTest
	public void testAddWhereValueVars()
	{
		final Var v = Var.alloc("v");
		Map<Object,List<?>> map = new HashMap<Object, List<?>>();
		
		map.put( Var.alloc("v"), Arrays.asList( "<one>", "<two>"));
		map.put( "?x", Arrays.asList( "three", "four"));
			
		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder =  whereClause.addWhereValueVars( map );
				
		Query query = builder.build();

		Var x = Var.alloc("x");
		ElementData edat = new ElementData();
		edat.add( x );
		edat.add( v );		
		BindingHashMap binding = new BindingHashMap();
		binding.add( v, NodeFactory.createURI( "one" ));
		binding.add( x, NodeFactory.createLiteral("three"));
		edat.add( binding );
		binding = new BindingHashMap();
		binding.add( v, NodeFactory.createURI( "two" ));
		binding.add( x, NodeFactory.createLiteral("four"));
		edat.add( binding );

		WhereValidator visitor = new WhereValidator( edat );
		query.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );
	}	

	@ContractTest
	public void testAddWhereValueRow_array()
	{
		final Var v = Var.alloc("v");
		final Var x = Var.alloc("x");
			
		WhereClause<?> whereClause = getProducer().newInstance();
		whereClause =  (WhereClause<?>) whereClause.addWhereValueVar( v );
		whereClause =  (WhereClause<?>) whereClause.addWhereValueVar( x );
		whereClause =  (WhereClause<?>) whereClause.addWhereValueRow( "<one>", "three" );
		AbstractQueryBuilder<?> builder =  whereClause.addWhereValueRow( "<two>", "four" );
		
		Query query = builder.build();

		ElementData edat = new ElementData();
		edat.add( v );
		edat.add( x );
		BindingHashMap binding = new BindingHashMap();
		binding.add( v, NodeFactory.createURI( "one" ));
		binding.add( x, NodeFactory.createLiteral("three"));
		edat.add( binding );
		binding = new BindingHashMap();
		binding.add( v, NodeFactory.createURI( "two" ));
		binding.add( x, NodeFactory.createLiteral("four"));
		edat.add( binding );

		WhereValidator visitor = new WhereValidator( edat );
		query.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );
	}	
	
	@ContractTest
	public void testAddWhereValueRow_collection()
	{
		final Var v = Var.alloc("v");
		final Var x = Var.alloc("x");
			
		WhereClause<?> whereClause = getProducer().newInstance();
		whereClause =  (WhereClause<?>) whereClause.addWhereValueVar( v );
		whereClause =  (WhereClause<?>) whereClause.addWhereValueVar( x );
		whereClause =  (WhereClause<?>) whereClause.addWhereValueRow( Arrays.asList("<one>", "three") );
		AbstractQueryBuilder<?> builder =  whereClause.addWhereValueRow( Arrays.asList("<two>", "four") );
		
		Query query = builder.build();

		ElementData edat = new ElementData();
		edat.add( v );
		edat.add( x );
		BindingHashMap binding = new BindingHashMap();
		binding.add( v, NodeFactory.createURI( "one" ));
		binding.add( x, NodeFactory.createLiteral("three"));
		edat.add( binding );
		binding = new BindingHashMap();
		binding.add( v, NodeFactory.createURI( "two" ));
		binding.add( x, NodeFactory.createLiteral("four"));
		edat.add( binding );

		WhereValidator visitor = new WhereValidator( edat );
		query.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );
	}	
	
	@ContractTest
	public void testSetVarsInWhereValues() throws ParseException {
		Var v = Var.alloc("v");
		Node value = NodeFactory.createLiteral(LiteralLabelFactory.createTypedLiteral(10));
		Map<Var, Node> values = new HashMap<>();
		values.put(v, value);

		WhereClause<?> whereClause = getProducer().newInstance();
		AbstractQueryBuilder<?> builder = whereClause.addWhereValueVar( "?x", "<one>", "?v");
		
		builder.setVar(v, value );
		
		Query query = builder.build();
		
		ElementData edat = new ElementData();
		Var x = Var.alloc("x");
		edat.add( x );
	
		BindingHashMap binding = new BindingHashMap();
		binding.add( x, NodeFactory.createURI( "one" ));
		edat.add( binding );
		binding = new BindingHashMap();
		binding.add( x, value);
		edat.add( binding );

		WhereValidator visitor = new WhereValidator( edat );
		query.getQueryPattern().visit( visitor );
		assertTrue( visitor.matching );
	}

	@ContractTest
	public void testDataQuery() {
		// test that the getVars getMap and clear methods work.
		Object o = "?x";
		
		WhereClause<?> whereClause = getProducer().newInstance();
		whereClause = (WhereClause<?>) whereClause.addWhereValueVar(o);
		whereClause = (WhereClause<?>) whereClause.addWhereValueVar("?y");
		whereClause = (WhereClause<?>) whereClause.addWhereValueRow("foo", "bar");
		whereClause = (WhereClause<?>) whereClause.addWhereValueRow("fu", null);

		assertFalse(whereClause.getWhereValuesVars().isEmpty());
		List<Var> lst = whereClause.getWhereValuesVars();
		assertEquals(2, lst.size());
		assertEquals(Var.alloc("x"), lst.get(0));
		assertEquals(Var.alloc("y"), lst.get(1));

		Map<Var, List<Node>> map = whereClause.getWhereValuesMap();
		assertEquals(2, map.keySet().size());
		List<Node> nodes = map.get(Var.alloc("x"));
		assertEquals(2, nodes.size());
		assertEquals(NodeFactory.createLiteral("foo"), nodes.get(0));
		assertEquals(NodeFactory.createLiteral("fu"), nodes.get(1));

		nodes = map.get(Var.alloc("y"));
		assertEquals(2, nodes.size());
		assertEquals(NodeFactory.createLiteral("bar"), nodes.get(0));
		assertNull(nodes.get(1));

		whereClause.clearWhereValues();

		assertTrue(whereClause.getWhereValuesVars().isEmpty());
		assertTrue(whereClause.getWhereValuesMap().isEmpty());

	}

}
