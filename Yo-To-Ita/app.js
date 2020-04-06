"use strict";
const Tree = require('./utils/BinaryTree');
const Node = require('./utils/Node');
const path = "./grammar.fcfg";

let numWords = 0;
let sentences = ["tu avrai novecento anni di età", "noi siamo illuminati", "tu hai amici lì"];
let g = fcfgToGrammar(path);

sentences.forEach((sentence) => {
    let s = tokenizeSentence(sentence);
    let t = parse_CKY(s, g);
    let itaToYoda = italianToYoda(t);
    console.log("-------");
    console.log("- Original sentence: " + sentence);
    console.log("- Italian to Yoda: "+ itaToYoda.dfsVisit(t.getRootNode()));
    console.log("-------\n");
});

/**
 * Funzione che prende un file .fcfg, lo parsifica e crea una grammatica nella forma:
 * '[corpo_della_produzione]' : [testa_della_produzione]
 *
 * @param path del file da parsificare
 */
function fcfgToGrammar(path) {
    let rules = require('fs').readFileSync(path, 'utf-8')
    .split('\n')
    .filter(l =>{return !l.startsWith('#') && !l.startsWith('%') && l !== ''});

    let grammar = {};
    for (let r in rules) {
        let parts = rules[r].split('->');

        let key = getKey(parts[1].trim().split(' '));
        let value = parts[0].trim();

        if(!grammar[key]){
            grammar[key] = [];
        }
        grammar[key].push(value);
    }
    return grammar;
}

function tokenizeSentence(sentence) {
    let res = sentence.replace(/\s{2,}/g," ").split(' ');
    numWords = res.length;
    return res;
}

function getKey(val){
    return JSON.stringify(val, null, 0);
}

/**
 * Funzione che crea una matrice quadrata NxN, dove N è passato
 * come parametro. Ogni casella della matrice viene inizializzata
 * con un oggetto nella forma:
 *
 * heads : [] -> Array con le teste delle produzioni
 * body: [] -> Array con i corpi delle produzioni
 * leftTree : [] -> Array con tutti i sottoalberi sinistri
 * rightTree : []  -> Array con tutti i sottoalberi destri
 * word : undefined -> La diagonale della matrice viene riempita con le
 * parole tokenizzate della frase analizzata in input
 *
 * N.B. Vengono utilizzati array in quanto l'esecuzione di CKY produce spesso
 * in una sola cella della matrice più di una riduzione. Gli indici in ogni array corrispondono
 * ad una specifica produzione.
 *
 * @param dim dimensione della matrice
 * @returns {Matrix}
 */
function createMatrix(dim) {
    let matrix = require('node-matrix');
    let m = matrix({ rows: dim, columns: dim});

    for(let i = 0; i < dim; i++){
        for(let j = 0; j < dim; j++){
            m[i][j] = {
                heads : [],
                body: [],
                leftTree : [],
                rightTree : [],
                word : undefined
            };
        }
    }
    return m;
}

/**
 * Funzione che prende in input una stringa e una grammatica e restituisce
 * l'albero di derivazione della stringa ottenuto tramite applicazione
 * dell'algoritmo CKY. Nel caso essa sia sintatticamente
 * corretta. In caso contrario,  restituisce un messaggio di errore.
 *
 * @param words stringa tokenizzata
 * @param grammar grammatica considerata
 * @returns {string|Tree} albero di derivazione della frase in input
 */
function parse_CKY(words, grammar){
    let numTokens = numWords + 1;
    let table = createMatrix(numTokens);

    for(let j=1; j < numTokens; j++){
        table[j-1][j].word = words[j-1];
        table[j-1][j].heads = grammar[getKey([words[j-1]])];

       for(let i = j-2; i >= 0; i--){
           for(let k = i+1; k < j; k++){

               let b = table[i][k].heads;
               let c = table[k][j].heads;

               for(let it_B = 0; it_B < b.length; it_B++){
                   for(let it_C = 0; it_C < c.length; it_C++){
                       
                       let roots = grammar[getKey([b[it_B],c[it_C]])];

                       if(roots !== undefined){
                           // per ogni regola che ha come corpo getKey([b[iterB],c[iterC]])
                           roots.forEach((r) =>{
                               table[i][j].heads.push(r);
                               table[i][j].body.push(getKey([b[it_B],c[it_C]]));
                               table[i][j].leftTree.push(table[i][k]);
                               table[i][j].rightTree.push(table[k][j]);
                           });
                       }
                   }
               }
           }
       }
    }
    if(table[0][numWords].heads.includes("S")){
        return new Tree(getTreeFromTable(table[0][numWords], "S"));
    }
    return "ATTENZIONE: Questa frase non è sintatticamente corretta";
}

/**
 * Questa funzione trasforma in maniera ricorsiva la matrice ottenuta dopo
 * l'esecuzione dell'algoritmo CKY in un albero binario. Partendo dal nodo in cui
 * l'esecuzione di CKY ha prodotto il risultato scivendo S, vengono ripercorse indietro
 * tutte le varie produzioni per ricostruire l'albero di derivazione ottenuto.
 *
 * @param node Nodo dell'algoritmo ricorsivo
 * @param production regola associata al nodo
 * @returns {Node} ritorna il nodo radice ell'albero così formato.
 */


function getTreeFromTable(node, production) {
    let index = node.heads.findIndex(i => i === production);
    let root = new Node(node.heads[index]);

    if(node.body.length !== 0) {
        let leftRight = JSON.parse(node.body[index]);

        if (leftRight[0]) {
            root.left = getTreeFromTable(node.leftTree[index], leftRight[0]);
        }
        if (leftRight[1]) {
            root.right = getTreeFromTable(node.rightTree[index], leftRight[1]);
        }
    }
    if(node.word){
        root.leaf = node.word;
    }
    return root;
}


/**
 * RULES:
 * S si riscrive sempre nella forma SVX
 * VP si riscrive sempre nella forma VX
 *
 * Dunque a sinistra di S ci sarà sempre il soggetto, mentre a destra
 * si troverà sempre verbo e il resto della frase
 * @param tree albero di derivazione della frase italiana su
 * cui viene eseguita la trasformazione
 * @returns {Tree} ritorna l'albero di derivazione Yoda
 */
function italianToYoda(tree){
    function svx_to_sxv(t) {
        let vp = t.getRootNode().right;
        let tmp = vp.left;
        vp.left = vp.right;
        vp.right = tmp;

        return t;
    }

    function sxv_to_xsv(t) {
        let vp = t.getRootNode().right;
        let tmp = vp.left;

        vp.left = t.getRootNode().left;
        t.getRootNode().left = tmp;
        return t;
    }

    let sxv = svx_to_sxv(tree);
    return sxv_to_xsv(sxv);
}



