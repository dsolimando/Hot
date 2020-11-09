package be.solidx.hot.data.jdbc.sql.criterion;

/*
 * #%L
 * Hot
 * %%
 * Copyright (C) 2010 - 2020 Solidx
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

import be.solidx.hot.data.criterion.KeyModifier;
import be.solidx.hot.data.jdbc.TableMetadata;

public class TablenameKeyModifier implements KeyModifier {

	private TableMetadata tableMetadata;
	
	public TablenameKeyModifier(TableMetadata tableMetadata) {
		this.tableMetadata = tableMetadata;
	}

	@Override
	public String modifyKey(String key) {
		if (key.split("\\.").length < 2) 
			key = tableMetadata.getName() + "." + key;
		
		if (tableMetadata.getSchema() != null)
			return tableMetadata.getSchema() + "." + key;
		return key;
	}
}
