var requiredOrganization = '{{requiredOrganization}}';
var dbName = '{{dbName}}';

db.getSiblingDB(dbName).getCollectionNames().forEach(function(collName) {
    db.getSiblingDB(dbName).getCollection(collName).find().toArray().forEach(
        function(obj) {
            var orgacode = obj['POR_ORGACODE'];
            if (orgacode !== requiredOrganization && orgacode !== undefined) {
                print(
                    db.getSiblingDB(dbName) + ' DB -> Deleting records of ' +
                    orgacode + ' From collection [' + collName + '] having ID --> [' + obj._id + ']'
                );
                print(db.getSiblingDB(dbName).getCollection(collName).remove({'_id': obj._id}));
            }
        }
    );
});

