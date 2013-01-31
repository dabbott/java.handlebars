java.handlebars
===============

A Java implementation of the [Handlebars] templating language. 

This project began as a small component of a Java webserver, but is complete enough to use wherever Handlebars-style template parsing is desired. Documentation and feature changes are currently in progress.

This implementation comes with two components:

* Handlebars template parser
* JSON wrapper

Built-in functions:

###### Handlebars:

* `if` .. `else` (conditionally compile certain blocks of the template)
* `each` (iterate over a JSON array)
* `with` (set the scope of identifier-lookup)
* `>` (parse sub-templates/partials)

###### Custom: 

* `eachIndex` (iterate over a JSON array, with additional identifiers $index, $first, $last, and $only)
* `eq_const` (compare a JSON value with a string/numeric constant)
* `escape` (escape HTML entities)
* `escapeAttr` (escape HTML-attribute entities)

#### Defining Additional Functions

Helper functions may be defined with
```java
registerHelper(String name, IHelper f)
```
where `name` is the name of the function, and `f` is the function definition. Helpers functions implement the `IHelper` class and have full access to the internals of the parser.

For example, the definition of the `if` helper:


```java
private static class If implements IHelper {

	public String apply(Stack<JObject> stack, ASTNode n, JSON j) {
		String output = "";

		// First, look up the name of the identifier in j, the current JSON context
		JSON v = Parser.lookup(stack, j, n.args.get(0).t.tag);

		// Next, test the value for "truthiness".
		// If the value is truthy, parse the "if" portion of the this block
		if (v != null && v.toString() != null && ! v.toString().equals("0")) {
			for (ASTNode child : n.children) {
				if (child.t.type == Type.Simple && child.t.tag.equals("else"))
					break;
				output += Parser.parse(stack, j, child);
			}

		// Otherwise, parse the "else" portion of this block, if one exists
		} else {
			// Search for an {{else}}, output any children after it's found
			Boolean found = false;
			for (ASTNode child : n.children) {
				if (found)
					output += Parser.parse(stack, j, child);
				if (! found && child.t.type == Type.Simple && child.t.tag.equals("else"))
					found = true;
			}
		}
		return output;
	}
}
```

##### Note:

This implementation does not currently support the entire Handlebars language, and certain features differ from the official JavaScript implementation. It was written to support certain very specific features, and certainly needs some work before use in a production environment.

##### Differences:

* Currently, HTML-entity escaping is disabled by default. `{{` does not escape HTML, and `{{{` is not implemented.
* There can be no spaces between delimiters and identifiers - e.g. `{{ #if test}}` will not compile, as it has spaces after the first pair of braces.
* When an identifier is not found in the current scope, this implementation will search for that identifier by automatically traversing the JSON data until reaching the root of the JSON object. Identifiers are looked up across sub-template boundaries.

Released under the MIT license.

[Handlebars]: http://handlebarsjs.com/
