/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mapclassworker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.LatLongUtils;
import org.mapsforge.core.util.Parameters;
import org.mapsforge.map.awt.graphics.AwtGraphicFactory;
import org.mapsforge.map.awt.util.AwtUtil;
import org.mapsforge.map.awt.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.datastore.MultiMapDataStore;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.debug.TileCoordinatesLayer;
import org.mapsforge.map.layer.debug.TileGridLayer;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik;
import org.mapsforge.map.layer.download.tilesource.TileSource;
import org.mapsforge.map.layer.hills.DiffuseLightShadingAlgorithm;
import org.mapsforge.map.layer.hills.HillsRenderConfig;
import org.mapsforge.map.layer.hills.MemoryCachingHgtReaderTileSource;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.model.IMapViewPosition;
import org.mapsforge.map.reader.MapFile;


/**
 *
 * @author felipe
 */
public class mapclass {
    
    
    private GraphicFactory GRAPHIC_FACTORY = AwtGraphicFactory.INSTANCE;
    
    private MapView mapView;
    private HillsRenderConfig hillsCfg = null;  
    private File demFolder=null;
        
    private boolean SHOW_DEBUG_LAYERS = false;
    private static final boolean SHOW_RASTER_MAP   = false;
    
    private boolean enable_tile_size = false;
    private int tileSize = 512;
    
    protected String  path_data;
    
    public mapclass()
    {       

    }
    
    private BoundingBox addLayers(MapView mapView, List<File> mapFiles, HillsRenderConfig hillsRenderConfig) {
        Layers layers = mapView.getLayerManager().getLayers();

        tileSize = enable_tile_size? tileSize : SHOW_RASTER_MAP ? 256 : 512;

        // Tile cache
        TileCache tileCache = AwtUtil.createTileCache(
                tileSize,
                mapView.getModel().frameBufferModel.getOverdrawFactor(),
                4096,
                new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString()));

        final BoundingBox boundingBox;
        if (SHOW_RASTER_MAP) {
            // Raster
            //This need a connection to server ... but i dont use iut 
            mapView.getModel().displayModel.setFixedTileSize(tileSize);
            TileSource tileSource = OpenStreetMapMapnik.INSTANCE;
            TileDownloadLayer tileDownloadLayer = createTileDownloadLayerv2(tileCache, mapView.getModel().mapViewPosition, tileSource);
            layers.add(tileDownloadLayer);
            tileDownloadLayer.start();
            mapView.setZoomLevelMin(tileSource.getZoomLevelMin());
            mapView.setZoomLevelMax(tileSource.getZoomLevelMax());
            boundingBox = new BoundingBox(LatLongUtils.LATITUDE_MIN, LatLongUtils.LONGITUDE_MIN, LatLongUtils.LATITUDE_MAX, LatLongUtils.LONGITUDE_MAX);
            
        } else {
            // Vector
            if(enable_tile_size)
            {
                mapView.getModel().displayModel.setFixedTileSize(tileSize);
            }
            
            MultiMapDataStore mapDataStore = new MultiMapDataStore(MultiMapDataStore.DataPolicy.RETURN_ALL);
            
            for (File file : mapFiles) {
                mapDataStore.addMapDataStore(new MapFile(file), true, true);
            }
                        
            TileRendererLayer tileRendererLayer = createTileRendererLayerv2(tileCache, mapDataStore, mapView.getModel().mapViewPosition, hillsRenderConfig);
           layers.add(tileRendererLayer);
            boundingBox = new BoundingBox(mapDataStore.boundingBox().minLatitude, mapDataStore.boundingBox().minLongitude, mapDataStore.boundingBox().maxLatitude, mapDataStore.boundingBox().maxLongitude);
        }
        
        /*
         I wish this could be different
        */
        mapView.setZoomLevelMin((byte) 0);
        mapView.setZoomLevelMax((byte) 22);

        // Debug
        if (SHOW_DEBUG_LAYERS) {
            layers.add(new TileGridLayer(GRAPHIC_FACTORY, mapView.getModel().displayModel));
            layers.add(new TileCoordinatesLayer(GRAPHIC_FACTORY, mapView.getModel().displayModel));
        }

        return boundingBox;
    }
    
     
   //@SuppressWarnings("unused")
   private TileDownloadLayer createTileDownloadLayerv2(TileCache tileCache, IMapViewPosition mapViewPosition , TileSource tileSource) 
   {
        return new TileDownloadLayer(tileCache, mapViewPosition, tileSource, GRAPHIC_FACTORY) {
             @Override
            public boolean onTap(LatLong tapLatLong, Point layerXY, Point tapXY) {
                System.out.println("Tap on: " + tapLatLong);          
                return true;
            }
        };
    }

    private TileRendererLayer createTileRendererLayerv2(TileCache tileCache, MapDataStore mapDataStore,IMapViewPosition mapViewPosition,HillsRenderConfig hillsRenderConfig )
    {
         TileRendererLayer tileRendererLayer = new TileRendererLayer(tileCache, mapDataStore, mapViewPosition, true, true, true, GRAPHIC_FACTORY, hillsRenderConfig) {
        
             @Override
            public boolean onTap(LatLong tapLatLong, Point layerXY, Point tapXY) {
                
                System.out.println("mark.data_info"+ tapLatLong);
                System.out.println("Zoom level --> " + mapViewPosition.getZoomLevel());
                
                return true;
            }
        };  
        
         
        tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.FEPS);
        return tileRendererLayer;
    }  
    
    
    public void set_path(String value)
    {
        path_data = value;
    }
    
    public void build(javax.swing.JPanel jPnMap)
    {
    
        mapView = new MapView();        
        mapView.setSize(jPnMap.getWidth(),jPnMap.getHeight());
        mapView.getFpsCounter().setVisible(true);

        ArrayList<File> MapFiles = new ArrayList();
        
        // Frame buffer HA2
        Parameters.ANTI_ALIASING = true;
        Parameters.FRAME_BUFFER_HA3 = true;
       // Parameters.VALIDATE_COORDINATES = true;
        // Multithreaded map rendering
        Parameters.NUMBER_OF_THREADS = 20;
        // Square frame buffer
        Parameters.LAYER_SCROLL_EVENT = true;
        Parameters.SQUARE_FRAME_BUFFER = true;
        
        if(path_data != null)
        {
            demFolder = new File(path_data);

            MemoryCachingHgtReaderTileSource tileSource = new MemoryCachingHgtReaderTileSource(demFolder, new DiffuseLightShadingAlgorithm(), AwtGraphicFactory.INSTANCE);
            tileSource.setEnableInterpolationOverlap(true);
            tileSource.setMainCacheSize(8);
            tileSource.setNeighborCacheSize(8);
            hillsCfg = new HillsRenderConfig(tileSource);
            hillsCfg.indexOnThread();
            MapFiles.add(demFolder);
        }
        
        addLayers(mapView, MapFiles, hillsCfg);
        jPnMap.add(mapView);        
    
    }
    
    public void set_debug()
    {
        SHOW_DEBUG_LAYERS = !SHOW_DEBUG_LAYERS;
    }

    public void set_tile_size(String value)
    {
        switch(value)
        {
            case "128":
            case "256":
            case "512":
            case "1024":
            case "2048":
            case "4096":
                enable_tile_size = true;
                tileSize = Integer.parseInt(value);
            break;    
            default:
                enable_tile_size = false;
            break;
        }     
    }
    
}
