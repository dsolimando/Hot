show.rest.get('/authenticate').then { req ->
    req.authenticate([
        'name': 'Martin Lamotte',
        'username': 'mlamotte',
        'id': 1,
        'email': 'martin@splendid.fr'
    ])
    'authenticated'
}

show.rest.get('/lolos').auth().then { req ->
    [
        result:[req.user]
    ]
}