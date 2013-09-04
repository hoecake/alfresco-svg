package svg.behaviour;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.apache.log4j.Logger;

public class SvgThumbnail extends TransactionListenerAdapter
	implements ContentServicePolicies.OnContentUpdatePolicy {
	private static final String IMAGE_SVG_XML = "image/svg+xml";
	private static final String KEY_POST_TXN_NODES_TO_THUMBNAIL = "SvgThumbnail.KEY_POST_TXN_NODES_TO_THUMBNAIL";
	/** Logger */
	private static Logger LOG = Logger.getLogger(SvgThumbnail.class);

	//private final PolicyComponent policyComponent;

	private final ServiceRegistry services;

	//private DocumentTransformListener txnListener;
	//private static final String KEY_NODE_FOR_TXN = "DocumentTransformBehaviourTxnKey";

	public SvgThumbnail(PolicyComponent policyComponent,
			ServiceRegistry serviceService) {
		super();
		//this.policyComponent = policyComponent;
		this.services = serviceService;
        policyComponent.bindClassBehaviour(OnContentUpdatePolicy.QNAME,
                this, new JavaBehaviour(this, "onContentUpdate"));
        if (LOG.isDebugEnabled()) LOG.debug("Up and listening");
	//	txnListener = new DocumentTransformListener();
	}

	@Override
	public void onContentUpdate(NodeRef nodeRef, boolean isnew) {
		if (LOG.isDebugEnabled()) LOG.debug("START - noderef: " + nodeRef + " (" + isnew +")");
		//NodeRef nodeRef = newChild.getChildRef();
		final NodeService ns = services.getNodeService();
		
		
//	
//		qname name = 
//		services.getthumbnailservice().createthumbnail(noderef, contentmodel.prop_content, image_svg_xml, arg3, "doclib");
//	}
	
    if (!ns.getType(nodeRef).equals(ContentModel.TYPE_THUMBNAIL)
            && ns.getProperty(nodeRef, ContentModel.PROP_CONTENT) != null)
    {
        // Bind this service to the transaction and add the node to the set of nodes to thumbnail post txn
        AlfrescoTransactionSupport.bindListener(this);
        getPostTxnNodesToThumbnail().add(nodeRef);
    } else {
    	if (LOG.isDebugEnabled()) LOG.debug("SKIPPING - noderef: " + nodeRef + " (" + isnew +")");
    }
}

/*
 * (non-Javadoc)
 * @see org.alfresco.repo.transaction.TransactionListenerAdapter#afterCommit()
 */
@Override
public void afterCommit()
{
	if (LOG.isDebugEnabled()) LOG.debug("afterCommit");
	final NodeService ns = services.getNodeService();
	final ThumbnailService ts = services.getThumbnailService();
	
    for (final NodeRef nodeRef : getPostTxnNodesToThumbnail())
    {
        if (!ns.exists(nodeRef))
        {
        	continue;
        }
        Serializable value = ns.getProperty(nodeRef, ContentModel.PROP_CONTENT);
        ContentData contentData = DefaultTypeConverter.INSTANCE.convert(ContentData.class, value);
        if (contentData != null)
        {
    		if (!IMAGE_SVG_XML.equals(contentData.getMimetype())) {
    			if (LOG.isDebugEnabled()) LOG.debug("Skipping non-SVG noderef:" + nodeRef);
    			continue;
    		}
            List<ThumbnailDefinition> thumbnailDefinitions = ts.getThumbnailRegistry()
              //  .getThumbnailDefinitions(contentData.getMimetype(), contentData.getSize());
            		.getThumbnailDefinitions();
            for (final ThumbnailDefinition thumbnailDefinition : thumbnailDefinitions)
            {
                final NodeRef existingThumbnail = ts.getThumbnailByName(nodeRef,
                        ContentModel.PROP_CONTENT, thumbnailDefinition.getName());
                try
                {
                    // Generate each thumbnail in its own transaction, so that we can recover if one of them goes wrong
                    services.getTransactionService().getRetryingTransactionHelper().doInTransaction(
                            new RetryingTransactionCallback<Object>()
                            {

                                public Object execute() throws Throwable
                                {
                                    if (existingThumbnail == null)
                                    {
                                        if (LOG.isDebugEnabled())
                                        {
                                            LOG.debug("Creating thumbnail \""
                                                    + thumbnailDefinition.getName() + "\" for node " + nodeRef.getId());
                                        }
                                        ts.createThumbnail(nodeRef,
                                                ContentModel.PROP_CONTENT, IMAGE_SVG_XML,
                                                thumbnailDefinition.getTransformationOptions(), thumbnailDefinition
                                                        .getName());
                                    }
                                    else
                                    {
                                        LOG.debug("Updating thumbnail \""
                                                + thumbnailDefinition.getName() + "\" for node " + nodeRef.getId());
                                        ts.updateThumbnail(existingThumbnail,
                                                thumbnailDefinition.getTransformationOptions());
                                    }
                                    return null;
                                }
                            }, false, true);
                }
                catch (Exception e)
                {
                    LOG.warn("Failed to generate thumbnail \"" + thumbnailDefinition.getName()
                            + "\" for node " + nodeRef.getId(), e);
                }
            }
        }
    }
}

/**
 * Gets the txn-bound set of nodes that need thumbnailing.
 * 
 * @return the set of nodes that need thumbnailing
 */
private Set<NodeRef> getPostTxnNodesToThumbnail()
{
    @SuppressWarnings("unchecked")
    Set<NodeRef> nodesToThumbnail = (Set<NodeRef>) AlfrescoTransactionSupport
            .getResource(SvgThumbnail.KEY_POST_TXN_NODES_TO_THUMBNAIL);
    if (nodesToThumbnail == null)
    {
        nodesToThumbnail = new LinkedHashSet<NodeRef>(11);
        AlfrescoTransactionSupport
                .bindResource(SvgThumbnail.KEY_POST_TXN_NODES_TO_THUMBNAIL, nodesToThumbnail);
    }
    return nodesToThumbnail;
}
//	private class DocumentTransformListener extends TransactionListenerAdapter {
//		//private final Logger LOG = SvgThumbnail.LOG;
//
//		/*
//		 * Equality and hashcode generation are left unimplemented; we expect to
//		 * only have a single instance of this class per action.
//		 */
//
//		/**
//		 * Get the action parameters from the transaction and rendition
//		 */
//		@Override
//		public void afterCommit() {
//			if (LOG.isDebugEnabled()) {
//				LOG.debug("afterCommit.");
//			}
//
//			final Map<ChildAssociationRef, String> txnList = TransactionalResourceHelper
//					.getMap(KEY_NODE_FOR_TXN);
//
//			// Start a *new* read-write transaction to audit in
//			
//			RetryingTransactionCallback<Void> renderCallback = new RetryingTransactionCallback<Void>() {
//				public Void execute(){
//					renderInTxn(txnList);
//					return null;
//				}
//			};
//			transactionService.getRetryingTransactionHelper().doInTransaction(
//					renderCallback, false, true);
//		}
//
//		private void renderInTxn(Map<ChildAssociationRef, String> txnList){
//			for (Map.Entry<ChildAssociationRef, String> entry : txnList.entrySet()) {
//				NodeRef nodeRef = entry.getKey().getChildRef();
//
//				// If the node is gone, then do nothing
//				if (!nodeService.exists(nodeRef)) {
//					LOG.error("Error creating rendition, node gone!");
//					continue;
//				}
//				
//				
//				
//
//			}
//
//		}

}
