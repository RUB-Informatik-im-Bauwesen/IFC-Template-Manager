package utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.apstex.ifctoolbox.ifc.IfcAxis2Placement2D;
import com.apstex.ifctoolbox.ifc.IfcAxis2Placement3D;
import com.apstex.ifctoolbox.ifc.IfcBuildingStorey;
import com.apstex.ifctoolbox.ifc.IfcCartesianPoint;
import com.apstex.ifctoolbox.ifc.IfcContext;
import com.apstex.ifctoolbox.ifc.IfcDefinitionSelect;
import com.apstex.ifctoolbox.ifc.IfcDirection;
import com.apstex.ifctoolbox.ifc.IfcDoor;
import com.apstex.ifctoolbox.ifc.IfcExtrudedAreaSolid;
import com.apstex.ifctoolbox.ifc.IfcGloballyUniqueId;
import com.apstex.ifctoolbox.ifc.IfcLabel;
import com.apstex.ifctoolbox.ifc.IfcLengthMeasure;
import com.apstex.ifctoolbox.ifc.IfcLocalPlacement;
import com.apstex.ifctoolbox.ifc.IfcPolyline;
import com.apstex.ifctoolbox.ifc.IfcPositiveLengthMeasure;
import com.apstex.ifctoolbox.ifc.IfcProduct;
import com.apstex.ifctoolbox.ifc.IfcProductDefinitionShape;
import com.apstex.ifctoolbox.ifc.IfcProductRepresentation;
import com.apstex.ifctoolbox.ifc.IfcProfileTypeEnum;
import com.apstex.ifctoolbox.ifc.IfcProject;
import com.apstex.ifctoolbox.ifc.IfcProjectLibrary;
import com.apstex.ifctoolbox.ifc.IfcPropertyEnumeration;
import com.apstex.ifctoolbox.ifc.IfcPropertySetTemplate;
import com.apstex.ifctoolbox.ifc.IfcReal;
import com.apstex.ifctoolbox.ifc.IfcRectangleProfileDef;
import com.apstex.ifctoolbox.ifc.IfcRelAssociatesLibrary;
import com.apstex.ifctoolbox.ifc.IfcRelContainedInSpatialStructure;
import com.apstex.ifctoolbox.ifc.IfcRelDeclares;
import com.apstex.ifctoolbox.ifc.IfcRepresentation;
import com.apstex.ifctoolbox.ifc.IfcRepresentationItem;
import com.apstex.ifctoolbox.ifc.IfcShapeRepresentation;
import com.apstex.ifctoolbox.ifc.IfcText;
import com.apstex.ifctoolbox.ifc.IfcWall;
import com.apstex.ifctoolbox.ifc.IfcWallTypeEnum;
import com.apstex.ifctoolbox.ifcmodel.IfcModel;
import com.apstex.ifctoolbox.ifcmodel.IfcModel.IfcSchema;
import com.apstex.step.core.ClassInterface;
import com.apstex.step.core.LIST;
import com.apstex.step.core.SET;
import com.apstex.step.guidcompressor.GuidCompressor;

import components.ApplicationFrame;
import components.templating.ItemContainer;
import components.templating.TemplateProjectTreeView;
import javafx.scene.control.TreeItem;

/**
 * 
 * @author Marcel Stepien
 *
 */
public class IfcUtilities {
	
	public static enum PROPERTYTYPES { LABEL, TEXT, INTEGER, REAL, LOGICAL, IDENTIFIER };
	
	private static ArrayList<ClassInterface> temporaryRemoved = new ArrayList();
	
	public static void removeFromModel(IfcModel model, ClassInterface instance) {
			
		if(instance instanceof IfcProjectLibrary) {
			IfcProjectLibrary obj = (IfcProjectLibrary)instance;
			removeFromModel(model, obj);
		}
		
		if(instance instanceof IfcRelAssociatesLibrary) {
			IfcRelAssociatesLibrary obj = (IfcRelAssociatesLibrary)instance;
			removeFromModel(model, obj);
		}
		
		if(instance instanceof IfcPropertySetTemplate) {
			IfcPropertySetTemplate obj = (IfcPropertySetTemplate)instance;
			removeFromModel(model, obj);
		}
	}

	private static void removeFromModel(IfcModel model, IfcProjectLibrary library) {

		ArrayList<ClassInterface> sets = new ArrayList<>();
		
		if(library.getDeclares_Inverse() != null) {
			for(IfcRelDeclares rel : library.getDeclares_Inverse()) {
				sets.addAll((Collection<? extends ClassInterface>)rel.getRelatedDefinitions());
			}
		}
		
		for(ClassInterface inter : sets) {
			if(inter instanceof IfcPropertySetTemplate) {
				removeFromModel(model, (IfcPropertySetTemplate)inter);
			}
		}
		
		model.removeObjects((Collection<ClassInterface>)library.getHasContext_Inverse());
		temporaryRemoved.addAll(library.getHasContext_Inverse());
			
		model.removeObject(library);
		temporaryRemoved.add(library);
	}
	
	private static void removeFromModel(IfcModel model, IfcPropertySetTemplate propertySet) {
		model.removeObjects((Collection<ClassInterface>)propertySet.getHasPropertyTemplates());
		temporaryRemoved.addAll(propertySet.getHasPropertyTemplates());
		
		Collection<ClassInterface> relCollection = (Collection<ClassInterface>)propertySet.getHasContext_Inverse();
		for(ClassInterface rel : relCollection) {
			if(!(rel instanceof IfcRelAssociatesLibrary)) {
				model.removeObject(rel);
				temporaryRemoved.add(rel);
			}
		}
		
		model.removeObject(propertySet);
		temporaryRemoved.add(propertySet);
	}
	
	private static void removeFromModel(IfcModel model, IfcRelAssociatesLibrary.Ifc4 assoLib) {
		for(IfcDefinitionSelect inter : assoLib.getRelatedObjects()) {
			if(inter instanceof IfcPropertySetTemplate) {
				removeFromModel(model, (IfcPropertySetTemplate)inter);
			}
		}
		
		model.removeObject((ClassInterface)assoLib.getRelatingLibrary());
		temporaryRemoved.add((ClassInterface)assoLib.getRelatingLibrary());
		
		model.removeObject(assoLib);
		temporaryRemoved.add(assoLib);
	}
	
	public static boolean isTemporaryRemoved(ClassInterface instance) {
		return temporaryRemoved.contains(instance);
	}
	
	public static void addEnumerationToTemplate(IfcPropertyEnumeration enumeration){
		ApplicationUtilities.template.addObject(enumeration);		
	}
	
	
	/**
	 * Read information from frame and create the IfcModel for template content. 
	 */
	public static boolean createDefaultProject() {
		
		//create template stepmodel
		IfcModel model = new IfcModel(IfcSchema.IFC4);
		
		/*
		IfcPerson person = new IfcPerson.Ifc4.Instance(
				new IfcIdentifier.Ifc4("PERSON_ID", true), //Identification
				new IfcLabel.Ifc4("DEFAULT_FAMILY_NAME", true), //FamilyName
				new IfcLabel.Ifc4("DEFAULT_PERSON_NAME", true), //GivenName
				new LIST<IfcLabel>(), //MiddleNames
				new LIST<IfcLabel>(), //PrefixTitels
				new LIST<IfcLabel>(), //SuffixTitles
				new LIST<IfcActorRole>(), //Roles
				new LIST<IfcAddress>() //Addresses
		);
		model.addObject(person);
		
		IfcOrganization organization = new IfcOrganization.Ifc4.Instance(
				new IfcIdentifier.Ifc4("ORGANIZATION_ID", true), //Identification
				new IfcLabel.Ifc4("ORGANIZATION_NAME", true), //Name
				new IfcText.Ifc4("ORGANIZATION_DESCRIPTION", true), //Description
				new LIST<IfcActorRole>(), //Roles
				new LIST<IfcAddress>() //Addresses
		);
		model.addObject(organization);
		
		IfcPersonAndOrganization.Ifc4 personAndorganization = new IfcPersonAndOrganization.Ifc4.Instance(
				person, //The Person
				organization, //The Organization
				new LIST<IfcActorRole>() //Roles
		);
		model.addObject(personAndorganization);
		
		IfcApplication application = new IfcApplication.Ifc4.Instance(
				organization, //Application Developer
				new IfcLabel.Ifc4("", true), //Version
				new IfcLabel.Ifc4("", true), //Application Full Name
				new IfcIdentifier.Ifc4("", true) //Application Identifier
		);
		model.addObject(application);
		
		IfcOwnerHistory history = new IfcOwnerHistory.Ifc4.Instance(
				personAndorganization, 
				application, 
				null, 
				new IfcChangeActionEnum.Ifc4(IfcChangeActionEnum.Ifc4.IfcChangeActionEnum_internal.NOTDEFINED), 
				null,
				null, 
				null, 
				new IfcTimeStamp.Ifc4((int)new Date().getTime()) 
		);
		model.addObject(history);
		*/
		
		IfcProject project = new IfcProject.Ifc4.Instance();
		project.setGlobalId(new IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(), true));
		//project.setOwnerHistory(history);
		project.setName(new IfcLabel.Ifc4("NewProject", true));
		project.setDescription(new IfcText.Ifc4("New project description", true));
		model.addObject(project);
		
		IfcRelDeclares relDeclares = new IfcRelDeclares.Ifc4.Instance();
		relDeclares.setGlobalId(new IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(), true));
		relDeclares.setDescription(new IfcText.Ifc4("Relation of project and default library.", true));
		relDeclares.setName(new IfcLabel.Ifc4("Default Library Relation", true));
		relDeclares.setRelatingContext((IfcContext)project);
		
		SET<IfcDefinitionSelect> selects = new SET<>();
//		selects.add((IfcDefinitionSelect)defLibrary);
	
		relDeclares.setRelatedDefinitions(selects);
		model.addObject(relDeclares);
		
//		ApplicationUtilities.defLibrary = defLibrary;
		
		IfcProjectLibrary.Ifc4 library = new IfcProjectLibrary.Ifc4.Instance();
		library.setGlobalId(new IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(), true));
		library.setDescription(new IfcText.Ifc4("Default library description", true));
		library.setName(new IfcLabel.Ifc4("Default Library", true));
		//library.setOwnerHistory(((IfcLibraryReference.Ifc4)libCon.getItem()).getOwnerHistory());
		model.addObject(library);
		
		IfcRelDeclares.Ifc4 relDec = new IfcRelDeclares.Ifc4.Instance();
		relDec.setGlobalId(new IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(), true));
		relDec.setName(new IfcLabel.Ifc4("DeafaultLibrary_LibRel", true));
		relDec.setDescription(new IfcText.Ifc4("Relation between Project and Default Library", true));
		
		if(model.getIfcProject() != null) {
			relDec.setRelatingContext((IfcContext.Ifc4)model.getIfcProject());
		}
		
		SET<IfcDefinitionSelect.Ifc4> set = new SET<>();
		set.add(library);
		relDec.setRelatedDefinitions(set);
		model.addObject(relDec);
		
		ApplicationUtilities.template = model;
		
		//RELOAD TREEVIEW
		//=============================================
		TemplateProjectTreeView applicationTemplateTree = TemplateProjectTreeView.getInstance();
		TreeItem<ItemContainer> root = applicationTemplateTree.createTemplate(ApplicationUtilities.template);
		ApplicationFrame.reloadProjectTree(root);
		//=============================================
		
		return true;
	}
	
	public static void createDoorFile(String path) {
		//create template stepmodel
		IfcModel model = new IfcModel(IfcSchema.IFC4);
		
		try {
			model.writeStepFile(new File(path));
			model.readStepFile(new File(path));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Collection<IfcBuildingStorey.Ifc4> coll = model.getCollection(IfcBuildingStorey.Ifc4.class);
		for(IfcBuildingStorey.Ifc4 storey : coll) {

			IfcDoor door = new IfcDoor.Ifc4.Instance();
			door.setGlobalId(new IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(), true));
			door.setDescription(new IfcText.Ifc4("Eine Tür"));
			door.setName(new IfcLabel.Ifc4("Tür"));
			model.addObject(door);
			
			SET<IfcProduct> products = new SET<IfcProduct>();
			products.add((IfcProduct)door);
			
			/*
			IfcRelAggregates aggregates = new IfcRelAggregates.Ifc4.Instance(new IfcGloballyUniqueId.Ifc4(),
					storey.getOwnerHistory(),
					new IfcLabel.Ifc4("Tür Relation"),
					new IfcText.Ifc4("Beschreibung"),
					storey,
					products);
			model.addObject(aggregates);
			*/
			IfcRelContainedInSpatialStructure containedInSpatialStructure = new IfcRelContainedInSpatialStructure.Ifc4.Instance(
					new IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(), true),
					storey.getOwnerHistory(),
					new IfcLabel.Ifc4("Tür Relation"),
					new IfcText.Ifc4("Beschreibung"),
					products,
					storey);
			model.addObject(containedInSpatialStructure);
			
		}
		
		try {
			model.writeStepFile(new File(path));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public static IfcModel createWallFile(String path) {
		//create template stepmodel
		IfcModel model = new IfcModel(IfcSchema.IFC4);
		
		try {
			model.readStepFile(new File(path));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Collection<IfcBuildingStorey.Ifc4> coll = model.getCollection(IfcBuildingStorey.Ifc4.class);
		for(IfcBuildingStorey.Ifc4 storey : coll) {

			
			SET<IfcProduct> products = new SET<IfcProduct>();
			products.add((IfcProduct)createWallFile(model, "Musterwand_V", "Diese Wand ist vollständig modelliert.", 5.0, 0.0));
			products.add((IfcProduct)createWallFile(model, "Musterwand_F", "Diese Wand ist fehlerhaft modelliert.", 5.0, 10.0));
			
			IfcRelContainedInSpatialStructure containedInSpatialStructure = new IfcRelContainedInSpatialStructure.Ifc4.Instance(
					new IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(), true),
					storey.getOwnerHistory(),
					new IfcLabel.Ifc4("Wand Relation"),
					new IfcText.Ifc4("Beschreibung"),
					products,
					storey);
			model.addObject(containedInSpatialStructure);
		}
		
		try {
			model.writeStepFile(new File("WallModel_Filled.ifc"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return model;
	}

	public static IfcWall createWallFile(IfcModel model, String name, String desc, double len, double offset) {
		IfcCartesianPoint.Ifc4 location = new IfcCartesianPoint.Ifc4.Instance(new LIST<IfcLengthMeasure>(
				new IfcLengthMeasure.Ifc4(0.0), 
				new IfcLengthMeasure.Ifc4(0.0), 
				new IfcLengthMeasure.Ifc4(0.0)
		));
		model.addObject(location);
		
		IfcDirection.Ifc4 directionA = new IfcDirection.Ifc4.Instance(new LIST<IfcReal>(new IfcReal.Ifc4(0.0), new IfcReal.Ifc4(0.0), new IfcReal.Ifc4(1.0)));
		IfcDirection.Ifc4 directionB = new IfcDirection.Ifc4.Instance(new LIST<IfcReal>(new IfcReal.Ifc4(-1.0), new IfcReal.Ifc4(0.0), new IfcReal.Ifc4(0.0)));
		model.addObject(directionA);
		model.addObject(directionB);
				
		IfcAxis2Placement3D.Ifc4 axis3D = new IfcAxis2Placement3D.Ifc4.Instance();
		axis3D.setLocation(location);
		axis3D.setAxis(directionA);
		axis3D.setRefDirection(directionB);
		model.addObject(axis3D);
		
		IfcLocalPlacement.Ifc4 placement = new IfcLocalPlacement.Ifc4.Instance();
		placement.setRelativePlacement(axis3D);
		model.addObject(placement);
		
		IfcCartesianPoint.Ifc4 start = new IfcCartesianPoint.Ifc4.Instance(new LIST<IfcLengthMeasure>(
				new IfcLengthMeasure.Ifc4(0.0), 
				new IfcLengthMeasure.Ifc4(0.0 + offset), 
				new IfcLengthMeasure.Ifc4(0.0)
		));
		model.addObject(start);
		
		IfcCartesianPoint.Ifc4 end = new IfcCartesianPoint.Ifc4.Instance(new LIST<IfcLengthMeasure>(
				new IfcLengthMeasure.Ifc4(len), 
				new IfcLengthMeasure.Ifc4(0.0 + offset), 
				new IfcLengthMeasure.Ifc4(0.0)
		));
		model.addObject(end);
		
		IfcPolyline.Ifc4 polyLine = new IfcPolyline.Ifc4.Instance(new LIST<IfcCartesianPoint>(start, end));
		model.addObject(polyLine);
		
		IfcShapeRepresentation.Ifc4 wallRep = new IfcShapeRepresentation.Ifc4.Instance();
		wallRep.setRepresentationIdentifier(new IfcLabel.Ifc4("Axis"));
		wallRep.setRepresentationType(new IfcLabel.Ifc4("Curve2D"));
		wallRep.setItems(new SET<IfcRepresentationItem>(polyLine));
		model.addObject(wallRep);
//		
//		IfcCartesianPoint.Ifc4 aPoint = new IfcCartesianPoint.Ifc4.Instance(new LIST<IfcLengthMeasure>(
//				new IfcLengthMeasure.Ifc4(29.0 + offset), 
//				new IfcLengthMeasure.Ifc4(0.0), 
//				new IfcLengthMeasure.Ifc4(0.0)
//		));
//		model.addObject(aPoint);
//		
//		IfcCartesianPoint.Ifc4 bPoint = new IfcCartesianPoint.Ifc4.Instance(new LIST<IfcLengthMeasure>(
//				new IfcLengthMeasure.Ifc4(29.0 + offset), 
//				new IfcLengthMeasure.Ifc4(len), 
//				new IfcLengthMeasure.Ifc4(0.0)
//		));
//		model.addObject(bPoint);
//		
//		IfcCartesianPoint.Ifc4 cPoint = new IfcCartesianPoint.Ifc4.Instance(new LIST<IfcLengthMeasure>(
//				new IfcLengthMeasure.Ifc4(31.0 + offset), 
//				new IfcLengthMeasure.Ifc4(len), 
//				new IfcLengthMeasure.Ifc4(0.0)
//		));
//		model.addObject(cPoint);
//
//		IfcCartesianPoint.Ifc4 dPoint = new IfcCartesianPoint.Ifc4.Instance(new LIST<IfcLengthMeasure>(
//				new IfcLengthMeasure.Ifc4(31.0 + offset), 
//				new IfcLengthMeasure.Ifc4(0.0), 
//				new IfcLengthMeasure.Ifc4(0.0)
//		));
//		model.addObject(dPoint);
//		
//		IfcPolyline.Ifc4 profileCurve = new IfcPolyline.Ifc4.Instance(new LIST<IfcCartesianPoint>(aPoint, bPoint, cPoint, dPoint));
//		model.addObject(profileCurve);
		
		//IfcArbitraryClosedProfileDef.Ifc4 profile = new IfcArbitraryClosedProfileDef.Ifc4.Instance();
		//profile.setProfileType(new IfcProfileTypeEnum.Ifc4(IfcProfileTypeEnum.Ifc4.IfcProfileTypeEnum_internal.AREA));
		//profile.setOuterCurve(profileCurve);
		//model.addObject(profile);
		
		IfcCartesianPoint.Ifc4 loc = new IfcCartesianPoint.Ifc4.Instance(new LIST<IfcLengthMeasure>(
				new IfcLengthMeasure.Ifc4(len/2), //X
				new IfcLengthMeasure.Ifc4(0.0 + offset),  //Y
				new IfcLengthMeasure.Ifc4(0.0) //Z
		));
		model.addObject(loc);
		
		IfcAxis2Placement2D.Ifc4 axis3D2 = new IfcAxis2Placement2D.Ifc4.Instance();
		axis3D2.setLocation(loc);
		axis3D2.setRefDirection(directionB);
		model.addObject(axis3D2);
		
		IfcRectangleProfileDef.Ifc4 profile = new IfcRectangleProfileDef.Ifc4.Instance();
		profile.setProfileType(new IfcProfileTypeEnum.Ifc4(IfcProfileTypeEnum.Ifc4.IfcProfileTypeEnum_internal.AREA));
		profile.setPosition(axis3D2);
		profile.setXDim(new IfcPositiveLengthMeasure.Ifc4(len));
		profile.setYDim(new IfcPositiveLengthMeasure.Ifc4(0.2));
		model.addObject(profile);
		
		IfcDirection.Ifc4 extrudeDirection = new IfcDirection.Ifc4.Instance(new LIST<IfcReal>(
				new IfcReal.Ifc4(0.0), 
				new IfcReal.Ifc4(0.0), 
				new IfcReal.Ifc4(1.0)
		));
		model.addObject(extrudeDirection);
		
		IfcExtrudedAreaSolid.Ifc4 extrusionSolid = new IfcExtrudedAreaSolid.Ifc4.Instance();
		extrusionSolid.setPosition(axis3D);
		extrusionSolid.setExtrudedDirection(extrudeDirection);
		extrusionSolid.setDepth(new IfcPositiveLengthMeasure.Ifc4(2.5));
		extrusionSolid.setSweptArea(profile);
		model.addObject(extrusionSolid);
		
		IfcShapeRepresentation.Ifc4 wallRepB = new IfcShapeRepresentation.Ifc4.Instance();
		wallRepB.setRepresentationIdentifier(new IfcLabel.Ifc4("Body"));
		wallRepB.setRepresentationType(new IfcLabel.Ifc4("SweptSolid"));
		wallRepB.setItems(new SET<IfcRepresentationItem>(extrusionSolid));
		model.addObject(wallRepB);
		
		IfcProductDefinitionShape.Ifc4 productRep = new IfcProductDefinitionShape.Ifc4.Instance();
		productRep.setName(new IfcLabel.Ifc4("ShapeRep"));
		productRep.setDescription(new IfcText.Ifc4("ShapeRep Desc"));
		productRep.setRepresentations(new LIST<IfcRepresentation>(wallRepB, wallRep));
		model.addObject(productRep);
		
		IfcWall.Ifc4 standardCase = new IfcWall.Ifc4.Instance();
		standardCase.setGlobalId(new IfcGloballyUniqueId.Ifc4(GuidCompressor.getNewIfcGloballyUniqueId(), true));
		standardCase.setName(new IfcLabel.Ifc4(name));
		standardCase.setDescription(new IfcText.Ifc4(desc));
		standardCase.setObjectPlacement(placement);
		standardCase.setRepresentation((IfcProductRepresentation)productRep);
		standardCase.setPredefinedType(new IfcWallTypeEnum.Ifc4(IfcWallTypeEnum.Ifc4.IfcWallTypeEnum_internal.SOLIDWALL));
		model.addObject(standardCase);
		
		return standardCase;
	}
}
