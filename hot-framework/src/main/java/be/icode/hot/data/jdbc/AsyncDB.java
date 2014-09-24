package be.icode.hot.data.jdbc;

import java.util.Map;

import be.icode.hot.data.CollectionMetadata;

public interface AsyncDB<CLOSURE, T extends Map<?,?>> extends be.icode.hot.data.AsyncDB<CLOSURE, T> {

	CollectionMetadata getCollectionMetadata(String name);
}
