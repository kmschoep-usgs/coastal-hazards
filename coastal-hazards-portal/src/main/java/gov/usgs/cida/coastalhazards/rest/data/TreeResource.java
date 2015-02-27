package gov.usgs.cida.coastalhazards.rest.data;

import gov.usgs.cida.coastalhazards.gson.adapter.ItemTreeAdapter;
import gov.usgs.cida.coastalhazards.jpa.ItemManager;
import gov.usgs.cida.coastalhazards.model.Item;
import gov.usgs.cida.coastalhazards.rest.data.util.ItemUtil;
import gov.usgs.cida.utilities.HTTPCachingUtil;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 *
 * @author Jordan Walker <jiwalker@usgs.gov>
 */
@Path("/tree")
public class TreeResource {

	@GET
	@Path("/item")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRootTrees(@Context Request request) {
		Response response = null;
		try (ItemManager itemManager = new ItemManager()) {
			List<Item> items = itemManager.loadRootItems();
			
			Item newestItem = ItemUtil.gatherNewest(items);
			Response unmodified = HTTPCachingUtil.checkModified(request, newestItem);
			if (unmodified != null) {
				response = unmodified;
			}
			else {
				Gson treeGson = new GsonBuilder().registerTypeAdapter(Item.class, new ItemTreeAdapter()).create();
				JsonObject root = new JsonObject();
				JsonArray rootItems = new JsonArray();

				for (Item item : items) {
					rootItems.add(treeGson.toJsonTree(item));
				}

				root.add("rootItems", rootItems);
				response = Response.ok(root.toString(), MediaType.APPLICATION_JSON_TYPE).lastModified(newestItem.getLastModified()).build();
			}
		}
		return response;
	}
	
	@GET
	@Path("/item/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTree(@PathParam("id") String id, @Context Request request) {
		Response response = null;
		try (ItemManager itemManager = new ItemManager()) {
			Item item = itemManager.load(id);
			if (item == null) {
				throw new NotFoundException();
			}
			else {
				Item newestItem = ItemUtil.gatherNewest(item);

				Response unmodified = HTTPCachingUtil.checkModified(request, newestItem);
				if (unmodified != null) {
					response = unmodified;
				}
				else {
					Gson treeGson = new GsonBuilder().registerTypeAdapter(Item.class, new ItemTreeAdapter()).create();
					String jsonResult = treeGson.toJson(item);
					response = Response.ok(jsonResult, MediaType.APPLICATION_JSON_TYPE).lastModified(newestItem.getLastModified()).build();
				}
			}
		}
		return response;

	}

}
