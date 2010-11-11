# JLESS

JLESS is a pure Java port of Alexis Sellier's [LESS](http://lesscss.org) abstraction for CSS, which was written in Ruby.  By using only Java to produce the CSS output, this library offers much faster LESS processing for your Java web application when compared with other approaches that wrap interpreters around the original Ruby or JavaScript source.

## Quick Start

JLESS can be used either as a command line tool during your build process, or it can be used at runtime to translate LESS content on demand.

    git clone git://github.com/jryans/jless.git
    cd jless
    mvn package

### Standalone Tool

    # Use -c if you'd like to minify the output
    java -jar target/jless-<version>-jar-with-dependencies.jar [-c] <input less file>

### Runtime Library

    LessProcessor processor = new LessProcessor();
    processor.setCompressionEnabled(true); // Minification is off by default
    String css = processor.process(<input stream>).toString();

## Features

At this time, not all of the features of the LESS language have been ported over:

* Supported
    * Variables
    * Mixins
    * Mixin Arguments
    * Nesting
* Unsupported
    * Math Operations
    * Accessors
    * Imports

## Support

Please file [issues](https://github.com/jryans/jless/issues) for any problems you encounter.

## Credits

* [Alexis Sellier](https://github.com/cloudhead): For the initial implementation of the [LESS](http://lesscss.org) framework
* [Mathias](https://github.com/sirthias): For the [parboiled](https://github.com/sirthias/parboiled) PEG library, which greatly simplified this port

## Related Projects 

* [LESS](https://github.com/cloudhead/less): The original LESS abstraction in Ruby
* [less.js](https://github.com/cloudhead/less.js): A newer implementation in JavaScript
* [lesscss-engine](https://github.com/asual/lesscss-engine): Wraps less.js with a JavaScript interpreter for use in Java applications

