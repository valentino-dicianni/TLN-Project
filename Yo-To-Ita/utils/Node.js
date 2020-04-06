"use strict";

class Node {

    constructor(data) {
        this.val = data;
        this.left = null;
        this.right = null;
        this.leaf = null;
    }

    isLeaf() {
        return this.left != null ;
    }
}
module.exports = Node;
