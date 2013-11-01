require 'rubygems'
require 'sequel'

class GraphGenerator
  class << self

    def run_generator(type)
      case type
      when 'random'
        random_graph
      else
        puts 'Unknown type. Program will close immediatly'
      end
    end

    def random_graph
      puts "Type number of points"
      n = gets.chomp.to_i
      puts "Type probability"
      p = gets.chomp.to_f
      matrix = Array.new(n) { Array.new(n, 0) }
      (0...n).each do |i|
        (i+1...n).each do |j|
          chance = rand
          if p > chance
            matrix[i][j] = 1
            matrix[j][i] = 1
          end
        end
      end
      write_to_db(matrix, 'random')
    end

    def write_to_db(matrix, type)
      db = Sequel.connect('sqlite://graph.db')
      n = matrix.size
      graphs = db[:graphs]
      nodes = ""
      edges = ""
      (0...n).each do |i|
        nodes << "#{i},#{i};"
        (i+1...n).each do |j|
          edges << "#{i},#{j},0;" if matrix[i][j] == 1
        end
      end
      puts 'Graph saved to db' if graphs.insert(nodes: nodes, edges: edges, type: type)
    end
  end
end

def main
  puts "Hello from GraphGenerator!!!"
  puts "Enter graph type (possible values: 'random')"
  type = gets.chomp
  GraphGenerator.run_generator(type)
end

main
