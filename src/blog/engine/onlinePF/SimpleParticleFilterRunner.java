/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package blog.engine.onlinePF;

import blog.engine.ParticleFilterRunner;
import blog.model.Evidence;
import blog.model.Model;
import java.util.Collection;
import java.util.Properties;

/**
 *
 * @author xiang
 */
public class SimpleParticleFilterRunner extends ParticleFilterRunner{

    
    public SimpleParticleFilterRunner(Model model, Properties particleFilterProperties){
            super(model, particleFilterProperties);
            
    }
    @Override
    public Evidence getEvidence() {
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection getQueries() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
