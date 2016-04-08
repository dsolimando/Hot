/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2016 Solidx
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
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
