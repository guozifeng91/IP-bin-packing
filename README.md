# IP-bin-packing

bin packing for irregular polygons in irregular comtainers.
![](https://github.com/guozifeng91/IP-bin-packing/blob/master/result/taihushi_IP.png)

result using [Abeysooriya et al., 2018](https://www.sciencedirect.com/science/article/pii/S0925527317302980)
![](https://github.com/guozifeng91/IP-bin-packing/blob/master/result/taihushi_Abey.png)

## 1. input
simple polygon via double array ([example](https://github.com/guozifeng91/IP-bin-packing/blob/master/java/src/layoutIP/test/TestPlan_HuaSet.java)).<br>
polygon with holes via 3dm file ([example](https://github.com/guozifeng91/IP-bin-packing/blob/master/java/src/layoutIP/test/TestPlan_FromFile.java)), in princeple boundary curves from any CAD drawing, but different libraries are required.<br>

## 2. output
the result would be a list of Layout instances<br>
obtain the placement index for each layout via
```Java
int[][] ps = layout.result;
```
it is a 2d array, the first dimension indicates how many polygons are in the layout, the second are the detailed information. for example assume we have the first polygon as
```Java
int[] p = ps[0];
```
we can access this polygon via
```Jave
DiscreteGeometry geo = layout.templates[p[0]].discreteShape[p[1]];
```
and access its position (translation) within the container via
```Java
Vec translation = layout.domain.getTranslationInDomain(geo, p[2], p[3]);
```
Then the final geometry would be the original geometry
```Java
geo.g
```
plus the obtained translation.
