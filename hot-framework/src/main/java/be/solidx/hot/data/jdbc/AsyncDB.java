package be.solidx.hot.data.jdbc;

import java.util.Map;

import be.solidx.hot.data.CollectionMetadata;

public interface AsyncDB<CLOSURE, T extends Map<?,?>> extends be.solidx.hot.data.AsyncDB<CLOSURE, T> {

	CollectionMetadata getCollectionMetadata(String name);
}
