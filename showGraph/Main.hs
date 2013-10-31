import Control.Applicative
import Control.Arrow
import Database.HDBC
import Database.HDBC.Sqlite3
import Data.List
import Data.List.Split
import Data.Graph.Inductive
-- import Data.Graph.Inductive.PatriciaTree
import Data.GraphViz

listToTuple3 [a,b,c] = (a,b,c)
listToTuple2 [a,b] = (a,b)

readDB :: IO [(String,String,String)]
readDB = do 
    c <- connectSqlite3 "../graph.db"
    map (listToTuple3 . map fromSql) <$> quickQuery c "SELECT test,nodes,edges FROM tests" []

parseCSV :: String -> [[String]]
parseCSV = map (splitOn ",") . splitOn ";" 


genNodes ::  String -> [LNode String]
genNodes = map ((read *** id) . listToTuple2) . parseCSV

genEdges ::  String -> [LEdge Int]
genEdges = map (listToTuple3 . map read) . parseCSV

--genGraph title nodes edges = graphviz title (mkGraph nodes edges) (1000, 2000) (100,100) Portrait 

main = do 
    ds <- readDB 
--    let vizReady =  genGraph ds
    mapM preview $ map (\(t,ns,es) -> mkGraph (genNodes ns) (genEdges es) :: Gr String Int) ds
