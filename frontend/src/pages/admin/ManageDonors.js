import React, { useEffect, useState, useCallback } from 'react';
import API from '../../api';
import { toast } from 'react-toastify';
import { FaTint } from 'react-icons/fa';
import { BLOOD_GROUPS } from '../../constants';

export default function ManageDonors() {
  const [donors, setDonors] = useState([]);
  const [pagination, setPagination] = useState(null);
  const [page, setPage] = useState(1);
  const [bloodGroup, setBloodGroup] = useState('');
  const [city, setCity] = useState('');

  const load = useCallback(async (p = page) => {
    const params = { page: p, per_page: 15 };
    if (bloodGroup) params.blood_group = bloodGroup;
    if (city) params.city = city;
    const r = await API.get('/admin/donors', { params });
    setDonors(r.data.donors);
    setPagination(r.data.pagination);
    setPage(p);
  }, [page, bloodGroup, city]);

  useEffect(() => { load(1); }, [bloodGroup]); // eslint-disable-line

  const toggle = async (id) => {
    try { await API.post(`/admin/user/${id}/toggle`); toast.success('Toggled'); load(); } catch (e) { toast.error('Failed'); }
  };

  return (
    <div className="page">
      <div className="container">
        <h1 className="section-title"><FaTint /> Manage Donors</h1>

        <div className="card" style={{ marginBottom: '1.5rem' }}>
          <form onSubmit={e => { e.preventDefault(); load(1); }} style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'end' }}>
            <div className="form-group">
              <label>Blood Group</label>
              <select className="form-control" value={bloodGroup} onChange={e => setBloodGroup(e.target.value)}>
                <option value="">All</option>
                {BLOOD_GROUPS.filter(Boolean).map(bg => <option key={bg} value={bg}>{bg}</option>)}
              </select>
            </div>
            <div className="form-group" style={{ flex: 1, minWidth: '200px' }}>
              <label>City</label>
              <input className="form-control" value={city} onChange={e => setCity(e.target.value)} placeholder="Filter by city..." />
            </div>
            <button type="submit" className="btn btn-primary" style={{ height: 'fit-content' }}>Filter</button>
          </form>
        </div>

        <div className="card">
          <div className="table-responsive">
            <table className="table">
              <thead><tr><th>Name</th><th>Blood Group</th><th>City</th><th>State</th><th>Phone</th><th>Available</th><th>Action</th></tr></thead>
              <tbody>
                {donors.map(d => (
                  <tr key={d.id}>
                    <td>{d.full_name}</td>
                    <td><span className="badge badge-danger">{d.blood_group}</span></td>
                    <td>{d.city}</td>
                    <td>{d.state}</td>
                    <td>{d.phone}</td>
                    <td>{d.is_available ? <span className="badge badge-success">Yes</span> : <span className="badge badge-secondary">No</span>}</td>
                    <td><button className="btn btn-sm btn-outline" onClick={() => toggle(d.user_id)}>Toggle Status</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {pagination && pagination.pages > 1 && (
            <div className="pagination">
              {page > 1 && <button className="btn btn-sm btn-outline" onClick={() => load(page - 1)}>Previous</button>}
              <span style={{ padding: '0.5rem' }}>Page {page} of {pagination.pages}</span>
              {page < pagination.pages && <button className="btn btn-sm btn-outline" onClick={() => load(page + 1)}>Next</button>}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
