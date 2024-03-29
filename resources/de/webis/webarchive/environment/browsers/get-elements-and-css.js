var nodes = [];

function isVisible(node) {
  if (node.nodeType == Node.TEXT_NODE) {
    return node.textContent.trim() != "";
  } else if (node.nodeType == Node.ELEMENT_NODE) {
    return !!(node.offsetWidth || node.offsetHeight || node.getClientRects().length);
  } else {
    return false;
  }
}

function addNode(id, path, xmin, ymin, xmax, ymax, text, css) {
  if (typeof text == "string") {
    text = text.trim().replace(/\n/g, "\\n");
  } else {
    text = "";
  }
  nodes.push(JSON.stringify({ID: id, xPath: path, position: [xmin, ymin, xmax, ymax], text: text, css: css}))
}

function getCss(element) {
  const styles = window.getComputedStyle(element);
  const cssArray = Array.from(styles).map(name => [name, styles.getPropertyValue(name)]);
  var css = {};
  cssArray.forEach(function(data){
    css[data[0]] = data[1]
  });
  return css;
}

function traverse(node, parentPath, nodeName, nodeNameNumber, recursive) {
  const nodePath = parentPath + "/" + nodeName + "[" + nodeNameNumber + "]";
  const nodeId = node.id ? node.id : "-";
  var nodeText;
  var nodeBox, xmin, xmax, ymin, ymax;
  if (node.nodeType == Node.TEXT_NODE) {
    nodeText = node.textContent;
    const range = document.createRange();
    range.selectNodeContents(node);
    nodeBox = range.getBoundingClientRect();
  } else {
    nodeText = node.innerText;
    nodeBox = node.getBoundingClientRect();
  }
  xmin = nodeBox.left;
  ymin = nodeBox.top;
  xmax = xmin + nodeBox.width;
  ymax = ymin + nodeBox.height;

  if (node.nodeType == Node.ELEMENT_NODE) {
    nodeCss = getCss(node);
  }

  addNode(nodeId, nodePath, xmin, ymin, xmax, ymax, nodeText, nodeCss);

  if (recursive && node.nodeType != Node.TEXT_NODE) {
    const counts = {};
    const children = node.childNodes;
    for (let c = 0; c < children.length; ++c) {
      const child = children[c];
      if (isVisible(child)) {
        var childName;
        if (child.nodeType == Node.TEXT_NODE) {
          childName = "text()";
        } else {
          childName = child.tagName;
        }

        if (typeof counts[childName] == "undefined") {
          counts[childName] = 1;
        } else {
          counts[childName] += 1;
        }
        traverse(child, nodePath, childName, counts[childName], true);
      }
    }
  }
}

traverse(document.getElementsByTagName("body")[0], "/HTML[1]", "BODY", 1, true);
 return (nodes.join("\n"));
