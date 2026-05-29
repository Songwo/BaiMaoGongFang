package com.bmgf.service.impl;
import com.bmgf.po.Hole;
import com.bmgf.dao.HoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class HoleService {
@Autowired
    private HoleRepository holeRepository;
    public List<Hole> findHole(){
        return holeRepository.findAllBy();
    }
    public void save(Hole hole){
        holeRepository.save(hole);
    }
    public void delete(String id){
        holeRepository.deleteById(id);
    }
    public Hole findById(String id) {
        if(holeRepository.findById(id).isEmpty()){
            return null;
        };
        return holeRepository.findById(id).get();
    }

}
