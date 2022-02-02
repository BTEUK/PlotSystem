package me.bteuk.plotsystem.utils;

import java.util.List;

import com.sk89q.worldedit.math.BlockVector2;

public class Point {

	public static BlockVector2 getAveragePoint(List<BlockVector2> points) {

		int size = points.size();
		double x = 0;
		double z = 0;

		for (BlockVector2 bv : points) {

			x += bv.getX()/size;
			z += bv.getZ()/size;

		}

		return (BlockVector2.at(x, z));

	}

	public double getShortestDistance(BlockVector2 a1, BlockVector2 a2, BlockVector2 b1, BlockVector2 b2) {

		double dis = Double.POSITIVE_INFINITY;
		double val;

		//If the lines are parrallel
		if (b1.getZ() == b2.getZ() && a1.getZ() == a2.getZ()) {
			if (b1.getZ()-a1.getZ()>2) {
				return (b1.getZ()-a1.getZ());
			} else {
				if (inSegmentParrallelZ(a1, a2, b1, b2)) {
					return(b1.getZ()-a1.getZ());
				}
			}
		}


		if (inSegment(a1, b1, b2)) {
			val = getDistance(a1, b1, b2);
			if (val < dis) {
				dis = val;
			}
		}

		if (inSegment(a2, b1, b2)) {
			val = getDistance(a2, b1, b2);
			if (val < dis) {
				dis = val;
			}
		}

		if (inSegment(b1, a1, a2)) {
			val = getDistance(b1, a1, a2);
			if (val < dis) {
				dis = val;
			}
		}

		if (inSegment(b2, a1, a2)) {
			val = getDistance(b2, a1, a2);
			if (val < dis) {
				dis = val;
			}
		}

		return dis;

	}

	public double getDistance(BlockVector2 p1, BlockVector2 a1, BlockVector2 a2) {
		//If a1 and a2 are on the same Z axis
		if (a1.getZ()==a2.getZ()) {
			return (p1.distance(BlockVector2.at(p1.getX(), a1.getZ())));
		}

		//If a1 and a2 are on the same X axis
		if (a1.getX()==a2.getX()) {
			return (p1.distance(BlockVector2.at(a1.getX(), p1.getZ())));
		}

		double a = getNormal(a1, a2);
		double b = getB(a, p1);
		BlockVector2 intersect = intersect(a1, a2, a, b);
		return (p1.distance(intersect));
	}

	public double getNormal(BlockVector2 p1, BlockVector2 p2) {
		double dx = ((double) (p1.getX()-p2.getX()))/(p1.getZ()-p2.getZ());
		return (-1/dx);		
	}

	public double getB(double a, BlockVector2 p1) {
		return ((p1.getZ()*-a)+p1.getX());		
	}

	public BlockVector2 intersect(BlockVector2 p1, BlockVector2 p2, double a1, double b1) {
		double a2 = ((double) (p1.getX()-p2.getX()))/(p1.getZ()-p2.getZ());
		double b2 = getB(a2, p1);
		double z = (b2-b1)/(a1-a2);
		double x = a1*z+b1;
		return (BlockVector2.at(x, z));
	}

	public boolean inSegment(BlockVector2 p1, BlockVector2 a1, BlockVector2 a2) {

		//If a1 and a2 are on the same Z axis
		if (a1.getZ()==a2.getZ()) {
			return (inSegmentZeroZ(p1, a1, a2));
		}

		//If a1 and a2 are on the same X axis
		if (a1.getX()==a2.getX()) {
			return (inSegmentZeroX(p1, a1, a2));
		}
		double a = getNormal(a1, a2);
		double b = getB(a, p1);
		BlockVector2 a3 = intersect(a1, a2, a, b);

		if (a1.getX()>=a2.getX()) {
			if (a3.getX()>a1.getX()) {
				return false;
			}
			if (a3.getX()<a2.getX()) {
				return false;
			}
		} else {
			if (a3.getX()>a2.getX()) {
				return false;
			}
			if (a3.getX()<a1.getX()) {
				return false;
			}
		}
		if (a1.getZ()>=a2.getZ()) {
			if (a3.getZ()>a1.getZ()) {
				return false;
			}
			if (a3.getZ()<a2.getZ()) {
				return false;
			}
		} else {
			if (a3.getZ()>a2.getZ()) {
				return false;
			}
			if (a3.getZ()<a1.getZ()) {
				return false;
			}
		}
		return true;
	}

	public boolean inSegmentParrallelZ(BlockVector2 a1, BlockVector2 a2, BlockVector2 b1, BlockVector2 b2) {

		if (a1.getX()>=a2.getX()) {
			if (b1.getX()<=a1.getX() && b1.getX()>= a2.getX()) {
				return true;
			}
			if (b2.getX()<=a1.getX() && b2.getX()>= a2.getX()) {
				return true;
			}
		} else {
			if (b1.getX()>=a1.getX() && b1.getX()<= a2.getX()) {
				return true;
			}
			if (b2.getX()>=a1.getX() && b2.getX()<= a2.getX()) {
				return true;
			}
		}

		if (b1.getX()>=b2.getX()) {
			if (a1.getX()<=b1.getX() && a1.getX()>= b2.getX()) {
				return true;
			}
			if (a2.getX()<=b1.getX() && a2.getX()>= b2.getX()) {
				return true;
			}
		} else {
			if (a1.getX()>=b1.getX() && a1.getX()<= b2.getX()) {
				return true;
			}
			if (a2.getX()>=b1.getX() && a2.getX()<= b2.getX()) {
				return true;
			}
		}

		return false;			

	}

	public boolean inSegmentZeroZ(BlockVector2 p1, BlockVector2 a1, BlockVector2 a2) {
		if (a1.getX()>=a2.getX()) {
			if (p1.getX()>a1.getX()) {
				return false;
			}
			if (p1.getX()<a2.getX()) {
				return false;
			}
		} else {
			if (p1.getX()>a2.getX()) {
				return false;
			}
			if (p1.getX()<a1.getX()) {
				return false;
			}
		}
		return true;
	}

	public boolean inSegmentZeroX(BlockVector2 p1, BlockVector2 a1, BlockVector2 a2) {
		if (a1.getZ()>=a2.getZ()) {
			if (p1.getZ()>a1.getZ()) {
				return false;
			}
			if (p1.getZ()<a2.getZ()) {
				return false;
			}
		} else {
			if (p1.getZ()>a2.getZ()) {
				return false;
			}
			if (p1.getZ()<a1.getZ()) {
				return false;
			}
		}
		return true;
	}

}
