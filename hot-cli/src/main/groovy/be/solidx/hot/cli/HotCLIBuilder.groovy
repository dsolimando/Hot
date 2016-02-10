package be.solidx.hot.cli


import org.apache.commons.cli.GnuParser
import org.apache.commons.cli.PosixParser
import org.codehaus.groovy.cli.GroovyPosixParser

class HotCLIBuilder extends CliBuilder {

	@Override
	public OptionAccessor parse(Object args) {
		if (expandArgumentFiles) args = expandArgumentFiles(args)
        if (!parser) {
            parser = posix == null ? new GroovyPosixParser() : posix == true ? new PosixParser() : new GnuParser()
        }
        return new OptionAccessor(parser.parse(options, args as String[], stopAtNonOption))
	}

}
