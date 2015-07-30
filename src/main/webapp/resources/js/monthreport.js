// Переводит объект в JSON
function itemToJSON(store, items) {
    var data = [];
    if (items && store) {
        for (var n = 0; n < items.length; ++n) {
            var json = {};
            var item = items[n];
            if (item) {
                // Determine the attributes we need to process.
                var attributes = store.getAttributes(item);
                if (attributes && attributes.length > 0) {
                    for (var i = 0; i < attributes.length; i++) {
                        var values = store.getValues(item, attributes[i]);
                        if (values) {
                            // Handle multivalued and single-valued attributes.
                            if (values.length > 1) {
                                json[attributes[i]] = [];
                                for (var j = 0; j < values.length; j++) {
                                    var value = values[j];
                                    // Check that the value isn't another item. If it is, process it as an item.
                                    if (store.isItem(value)) {
                                        json[attributes[i]].push(dojo.fromJson(itemToJSON(store, value)));
                                    } else {
                                        json[attributes[i]].push(value);
                                    }
                                }
                            } else {
                                if (store.isItem(values[0])) {
                                    json[attributes[i]] = dojo.fromJson(itemToJSON(store, values[0]));
                                } else {
                                    json[attributes[i]] = values[0];
                                }
                            }
                        }
                    }
                }
                data.push(dojo.toJson(json));
            }
        }
    }
    return data;
}

