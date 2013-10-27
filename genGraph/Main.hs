import System.Environment (getArgs)
import System.Random
import Control.Applicative
import Data.List
import Data.Maybe
import Data.Graph.Inductive.Graph
import Data.Graph.Inductive.Tree
import Database.HDBC
import Database.HDBC.Sqlite3
import Debug.Trace

rtrace :: (Show a) => a -> a
rtrace = trace <$> show <*> id

labelNodes :: [Node] -> [LNode Int]
labelNodes = zip <$> id <*> id

genNodes :: Int -> [Node]
genNodes n = [0..n-1]

lToHConnect :: [Node] -> [LEdge Int]
lToHConnect l = [(x,y,0)| (x:xs) <- tails l, y <- xs]
htoLConnect l = lToHConnect (reverse l)
fullConnect l = lToHConnect l ++ htoLConnect l

reverseEdges :: [LEdge a] -> [LEdge a]
reverseEdges = map (\(a,b,c) -> (b,a,c))

showNodes :: [LNode Int] -> String
showNodes = concat . intersperse ";" . map (\(i,n) -> show i ++ ","++ show n)
showEdges :: [LEdge Int] -> String
showEdges = concat . intersperse ";" . map (\(a,b,n) -> show a++"," ++ show b ++ "," ++ show n)

main = getArgs >>= generateGraph . map read >>= saveToDB "../graph.db"

generateGraph :: [Int] -> IO (String, String, String)
generateGraph (n:l:_) = let ns = genNodes n
                            es = lToHConnect ns
                            p = 1 - fromIntegral l / fromIntegral (n*(n-1))*2 :: Double
                            trimmedEdges = catMaybes . zipWith (\i r -> if r < p then Nothing else Just i) es. randoms
                        in do g <- newStdGen
                              return (showNodes (labelNodes ns), showEdges (trimmedEdges g), "random")
generateGraph _ = error "Usage: genGraph <nodes> <edges>"        

saveToDB db (ns, es, t) = do
    c <- connectSqlite3 db
    run c "INSERT INTO graphs (nodes, edges, type) VALUES (?, ?, ?)" [toSql ns, toSql es, toSql t]
    commit c >> disconnect c
