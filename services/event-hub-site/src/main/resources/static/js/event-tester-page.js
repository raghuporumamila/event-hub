(function () {
  function generateSampleFromSchema(schema) {
    if (!schema || typeof schema !== 'object') {
      return {};
    }
    var type = schema.type;
    if (type === 'object' || schema.properties) {
      var result = {};
      var props = schema.properties || {};
      Object.keys(props).forEach(function (key) {
        result[key] = generateSampleFromSchema(props[key]);
      });
      return result;
    }
    if (type === 'array') {
      var itemSchema = schema.items;
      return itemSchema ? [generateSampleFromSchema(itemSchema)] : [];
    }
    if (type === 'string') {
      if (schema.const !== undefined) {
        return schema.const;
      }
      if (schema.format === 'date-time') {
        return new Date().toISOString();
      }
      if (schema.format === 'date') {
        return new Date().toISOString().slice(0, 10);
      }
      if (schema.enum && schema.enum.length > 0) {
        return schema.enum[0];
      }
      if (schema.examples && schema.examples.length > 0) {
        return schema.examples[0];
      }
      return '';
    }
    if (type === 'number' || type === 'integer') {
      return schema.minimum !== undefined ? schema.minimum : 0;
    }
    if (type === 'boolean') {
      return false;
    }
    if (type === 'null') {
      return null;
    }
    return '';
  }

  function populatePayloadFromSelected() {
    var select = document.getElementById('eventTesterDefinitionId');
    if (!select) {
      return;
    }
    var selected = select.options[select.selectedIndex];
    if (!selected) {
      return;
    }
    var schemaJson = selected.getAttribute('data-schema');
    if (!schemaJson) {
      return;
    }
    try {
      var schema = JSON.parse(schemaJson);
      var sample = generateSampleFromSchema(schema);
      document.getElementById('jsonData').value = JSON.stringify(sample, null, 2);
    } catch (e) {
      // Schema is not valid JSON; leave textarea as-is.
    }
  }

  var select = document.getElementById('eventTesterDefinitionId');
  if (select) {
    select.addEventListener('change', populatePayloadFromSelected);
    populatePayloadFromSelected();
  }
})();
