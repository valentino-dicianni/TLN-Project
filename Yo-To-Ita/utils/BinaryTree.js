"use strict";

class Tree {
    constructor(root){
        this.root = root;
    }

    getRootNode() {
        return this.root;
    }

    dfsVisit(node){
        let s = "";
        if(node.leaf){
            return node.leaf;
        }
        if(node.left){
            s = s.concat(" ", this.dfsVisit(node.left));
        }
        if(node.right){
            s = s.concat(" ", this.dfsVisit(node.right));
        }
        return s;
    }
}


module.exports = Tree;

