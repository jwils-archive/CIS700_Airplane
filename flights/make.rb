class Point
  attr_accessor :x, :y
  def initialize(x, y)
    @x = x.to_f
    @y = y.to_f
  end
  def scalar_multiply(factor)
    Point.new x * factor, y * factor
  end
  def multiply(pt)
    Point.new x * pt.x, y * pt.y
  end
  def add(pt)
    Point.new x + pt.x, y + pt.y
  end
  def minus(pt)
    Point.new x - pt.x, y - pt.y
  end
  def to_s
    "#{x.round(2)},#{y.round(2)}"
  end
end
class Path
  attr_accessor :src, :des, :start
  def initialize(src, des, start=0)
    @src = src
    @des = des
    @start = start
  end
  def delay(interval)
    pt = copy
    pt.delay! interval
    pt
  end
  def delay!(interval)
    @start = start + interval
  end
  def copy
    Path.new src, des, start
  end
  def to_s
    "#{src};#{des};#{start}"
  end
end
class PathBuilder
  def self.paths_from_points(pt1, pt2)
    [Path.new(pt2, pt1), Path.new(pt1, pt2)]
  end
end

class Board
  attr_accessor :paths
  def to_s
    paths.join("\n")
  end
  def pt_has_same_x_and_y(pt)
    pt.x == pt.y
  end

  def path_has_pt_having_same_x_and_y(path)
    pt_has_same_x_and_y(path.src) || pt_has_same_x_and_y(path.des)
  end

  def delay_paths_by!(interval)
    paths.each do |p|
      p.delay! interval
    end
  end
end

class TightCircleBoard < Board
  def min
    Point.new 0, 0
  end

  def max
    Point.new 94.9, 94.9
  end

  def spacing
    Point.new 20, 20
  end

  def xs
    rtn = []
    min.x.step(max.x, spacing.x) {|x| rtn << x}
    rtn = rtn.select {|x| x <= pt_limit.x}

    rtn << pt_limit.x - spacing.x/2.0
    rtn << pt_limit.x - 1.2

    rtn
  end

  def radius
    pt_limit.x
  end

  def ys
    # x^2 + y^2 = r^2
    xs.map do |x|
      Math.sqrt(radius ** 2 - x ** 2)
    end
  end

  def pt_limit
    Point.new 30, 30
  end

  def paths
    return @paths unless @paths.nil?

    limit = pt_limit
    x_coords = xs.select { |x| x <= limit.x }
    y_coords = ys.select { |y| y <= limit.y }

    pts = []
    x_coords.zip(y_coords) do |coord|
      x_pt, y_pt = coord
      pts << Point.new(x_pt, y_pt)
    end

    paths = []
    # handles first and fourth quadrant (1st quad is upper left)
    #
    shift = Point.new 20, 20
    add_50 = pt_limit.add shift
    pts.each do |pt|
      neg_pt = pt.scalar_multiply -1
      neg_pt = neg_pt.add add_50
      pt = pt.add add_50
      paths.concat PathBuilder.paths_from_points pt, neg_pt
    end

    sec_pt = Point.new 1, -1
    sec_quad_pts = pts.map do |pt|
      pt.multiply sec_pt
    end

    sec_quad_pts.each do |pt|
      other = pt.scalar_multiply -1
      other = other.add add_50
      pt = pt.add add_50
      paths.concat PathBuilder.paths_from_points pt, other
    end

    @paths = paths
  end
end

class TightGridBoard < Board
  def paths
    return @paths unless @paths.nil?

    min = Point.new 5.1, 5.1
    max = Point.new 94.9, 94.9
    spacing = Point.new 15, 15

    paths = []
    min.x.step(max.x, spacing.x) do |x|
      top = Point.new x, min.y
      bottom = Point.new x, max.y
      paths.concat PathBuilder.paths_from_points top, bottom
    end

    min.y.step(max.y, spacing.y) do |y|
      left = Point.new min.x, y
      right = Point.new max.x, y
      paths.concat PathBuilder.paths_from_points left, right
    end

    paths = paths.reject do |path|
      path_has_pt_having_same_x_and_y path
    end

    @paths = paths
  end
end

class TightCircleStaggeredBoard < TightCircleBoard
  attr_accessor :interval
  def initialize
    @interval = 6
  end
  def paths
    return @paths unless @paths.nil?

    paths = super
    accumulator = 0
    paths.each do |p|
      p.delay! accumulator
      accumulator += interval
    end
    
    last = paths
    2.times do
      last = last.map {|p| p.delay 100 }
      paths.concat last
    end

    @paths = paths
  end
end
#puts TightCircleStaggeredBoard.new

board = TightCircleBoard.new
puts board
puts TightGridBoard.new

#board.delay_paths_by! 100
#puts board

