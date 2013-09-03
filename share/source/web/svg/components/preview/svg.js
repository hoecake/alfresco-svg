/**
 * Copyright (C) 2010-2012 Share Extras Contributors.
 */

/**
 * This is the "SVG" plugin used to display documents using the google-code-SVG project.
 *
 * It supports any text-based format such as XML/HTML mark-up, source code and CSS that are supported
 * by google code SVG. See the SVG project site for more information.
 * 
 * @namespace Alfresco.WebPreview.prototype.Plugins
 * @class Alfresco.WebPreview.prototype.Plugins.SVG
 * @author Rasmus Melgaard
 */
function resizeIframe(obj)
    {
  	    //obj.style.height = obj.contentWindow.document.body.scrollHeight + 'px';
	obj.style.height = Math.ceil(obj.contentWindow.document.rootElement.height.baseVal.value) + 'px';
	obj.style.width = Math.ceil(obj.contentWindow.document.rootElement.width.baseVal.value) + 'px';
	  };
	  
(function()
{
	
   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML;
   
   /**
    * SVG web-preview plugin constructor
    *
    * @constructor
    * @param wp {Alfresco.WebPreview} The Alfresco.WebPreview instance that decides which plugin to use
    * @param attributes {Object} Arbitrary attributes brought in from the <plugin> element
    * @return {Alfresco.WebPreview.prototype.Plugins.SVG} Plugin instance
    */
   Alfresco.WebPreview.prototype.Plugins.SVG = function(wp, attributes)
   {
      this.wp = wp;
      this.attributes = YAHOO.lang.merge(Alfresco.util.deepCopy(this.attributes), attributes);
      return this;
   };
   
   Alfresco.WebPreview.prototype.Plugins.SVG.prototype =
   {
      /**
       * Attributes
       */
      attributes:
      {
         /**
          * Language abbreviation code to force display of a specific language, e.g. 'lang-html'
          * 
          * If not specified (or empty) will use the normal SVG auto-detection
          * 
          * @property lang
          * @type String
          * @default ""
          */
         lang: ""
      },
   
      /**
       * Tests if the plugin can be used in the users browser.
       *
       * @method report
       * @return {String} Return nothing if the plugin may be used, otherwise returns a message containing the reason
       *         it can't be used as a string.
       * @public
       */
      report: function SVG_report()
      {
    	  //maybee http://code.google.com/p/svgweb/
         if (document.implementation.hasFeature("http://www.w3.org/TR/SVG11/feature#Shape", "1.1")) return;
         return "SVG version 1.1 not supported, tested on feature: 'http://www.w3.org/TR/SVG11/feature#Shape'";
      },
      
      /**
       * Display the node.
       *
       * @method display
       * @public
       */
      display: function SVG_display()
      {
  		
          var src = this.wp.getContentUrl();
          return '<br/><div style="margin: 0 auto;width:100%;text-align:center;display:inline-block;float:none;"><iframe allowfullscreen="true" width="100%" src="' + src + '" name="' + this.wp.options.name + '" frameborder="0" scrolling="auto" onload="javascript:resizeIframe(this);"></iframe></div>';

      },
      /**
       * Required YUI components have been loaded
       * 
       * @method onComponentsLoaded
       * @public
       */
//      onComponentsLoaded : function Embed_onComponentsLoaded()
//      {
//    	  var url = this.attributes.src ? this.wp.getThumbnailUrl(this.attributes.src) : this.wp.getContentUrl(), displaysource, previewHeight;
//    
//         
//      }
      

   };
})();