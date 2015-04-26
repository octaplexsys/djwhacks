// maze.scala
// pure scala
// just run with:
// scala maze.scala

val width=30
val height=8

val rng = new scala.util.Random

type Loc=(Int,Int)
type Edge=(Loc,Loc)

def neighbours(loc: Loc): Set[Edge] = loc match {
 case (x,y) => Set( ((x,y),(x,y-1)),
                    ((x,y),(x,y+1)),
                    ((x,y),(x-1,y)),
                    ((x,y),(x+1,y)) )
}

def to(edge: Edge): Loc = edge match { case(n1,n2) => n2 }

def inrange(loc: Loc): Boolean = loc match {
 case (x,y) => ( (x>=0) & (y>=0) & (x<width) & (y<height) )
}

def extensions(tree: Set[Edge]): Set[Edge] = {
 val locs=tree.flatMap{ e => Set(e._1,e._2) }
 val allEdges=locs.flatMap{neighbours(_)}
 val newEdges=allEdges.filter { e => {
                 val l=to(e)
                 inrange(l) & !locs.contains(l) 
                 } }
 newEdges
}

def pick[T](s: Set[T]):T = s.toList(rng.nextInt(s.size))

@annotation.tailrec def genTree(currTree: Set[Edge]): Set[Edge] = {
 val newEdges=extensions(currTree)
 if (newEdges.size == 0) { currTree } else {
  genTree(currTree + pick(newEdges))
 }
}

// test code

val init=Set( ((0,0),(0,1)) , ((0,1),(1,1)) )

val tree=genTree(init)

def contains(e: Edge): Boolean = e match {
 case (l1,l2) => ((tree.contains(l1,l2))|(tree.contains(l2,l1)))
}

val maze=for {
 y <- 0 until height
 z <- 0 until 2
 x <- 0 until width
 str = if (z==0) {
  if (contains(((x,y),(x,y-1)))) "+ " else "+-"
 } else {
  if (contains(((x,y),(x-1,y)))) "  " else "| "
 }
 termStr = if (x==width-1) {
    if (z==0) str+"+\n" else str+"|\n"
   } 
  else str
} yield termStr

val mazeFinal=maze ++ ( for (x <- 0 until width) yield "+-" )
val mazeStr=mazeFinal.reduce(_+_) + "+\n"

println(mazeStr)


// eof


